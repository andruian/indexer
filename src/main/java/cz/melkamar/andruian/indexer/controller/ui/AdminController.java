package cz.melkamar.andruian.indexer.controller.ui;

import cz.melkamar.andruian.ddfparser.exception.DataDefFormatException;
import cz.melkamar.andruian.ddfparser.exception.RdfFormatException;
import cz.melkamar.andruian.ddfparser.model.DataDef;
import cz.melkamar.andruian.indexer.controller.Util;
import cz.melkamar.andruian.indexer.dao.MongoDataDefFileRepository;
import cz.melkamar.andruian.indexer.model.DataDefFile;
import cz.melkamar.andruian.indexer.net.DataDefFetcher;
import cz.melkamar.andruian.indexer.service.IndexService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.thymeleaf.util.StringUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

/**
 * A controller for all admin-related UI actions.
 */
@Controller
@RequestMapping("/admin")
public class AdminController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AdminController.class);
    private final IndexService indexService;
    private final DataDefFetcher dataDefFetcher;
    private final MongoDataDefFileRepository dataDefFileRepository;

    @Autowired
    public AdminController(IndexService indexService,
                           DataDefFetcher dataDefFetcher,
                           MongoDataDefFileRepository dataDefFileRepository) {
        this.indexService = indexService;
        this.dataDefFetcher = dataDefFetcher;
        this.dataDefFileRepository = dataDefFileRepository;
    }

    /**
     * Adds default attributes that are expected to be sent with each request to the admin page.
     *
     * That includes the module name, the data definitions registered in the system and running/finished indexing jobs.
     */
    @ModelAttribute
    public void addAttributes(Model model) {
        model.addAttribute("module", "admin");
        List<DataDefFile> dataDefFiles = dataDefFileRepository.findAll();
        List<DataDefFileTableRow> dataDefFileTableRows = new ArrayList<>(dataDefFiles.size());

        for (DataDefFile dataDefFile : dataDefFiles) {
            int count = 0;
            for (String dataDefIri : dataDefFile.getDataDefIris()) {
                count += indexService.getIndexedPlacesCount(dataDefIri);
            }
            dataDefFileTableRows.add(new DataDefFileTableRow(dataDefFile, count));
        }

        IndexingJobsStatus indexingJobsStatus = new IndexingJobsStatus();
        indexingJobsStatus.addAllRunningJobs(indexService.getRunningJobs());
        indexingJobsStatus.addAllFinishedJobs(indexService.getFinishedJobs());
        for (Map.Entry<DataDef, Exception> entry : indexService.getErroredJobs().entrySet()) {
            indexingJobsStatus.addErrorJob(entry.getKey(), entry.getValue().getMessage());
        }

        model.addAttribute("datadefs", dataDefFileTableRows);
        model.addAttribute("indexingJobs", indexingJobsStatus);

        Util.addPrincipalAttribute(model);
    }

    /**
     * An endpoint method for showing the admin interface.
     */
    @GetMapping
    public String admin(Model model) {
        if (!model.containsAttribute("datadefParam"))
            model.addAttribute("datadefParam", new DataDefFileParam());


        return "admin";
    }

    /**
     * An endpoint method for reindexing data definitions from the web GUI. It is equivalent to the
     * REST API method {@link cz.melkamar.andruian.indexer.controller.rest.AdminRestController#reindex(String, boolean)}.
     *
     * @param dataDefFileParam The form parameter specifying which data definition to reindex.
     * @param attributes       Spring MVC attributes for the redirect back to the admin page after reindexing has started.
     */
    @PostMapping("/reindex")
    public String reindex(@ModelAttribute("reindexOptions") DataDefFileParam dataDefFileParam,
                          RedirectAttributes attributes) {
        LOGGER.info("reindex " + dataDefFileParam);

        Status status = new Status();
        if (dataDefFileParam.reindexAll) {
            status.setOk(true);
            status.setMessage(StringUtils.capitalize(dataDefFileParam.reindexType) + " reindexing started for all data sources.");
            indexService.reindexAll(dataDefFileParam.reindexType.equals("full"));
        } else {
            try {
                List<DataDef> dataDefs = dataDefFetcher.getDataDefsFromUri(dataDefFileParam.getFileUrl());
                status.setOk(true);
                status.setMessage(StringUtils.capitalize(dataDefFileParam.reindexType) + " reindexing started for " + dataDefFileParam.fileUrl);
                for (DataDef dataDef : dataDefs) {
                    indexService.indexDataDef(dataDef, dataDefFileParam.reindexType.equals("full"));
                }
            } catch (RdfFormatException | DataDefFormatException | IOException e) {
                LOGGER.error("An exception occurred when pulling datadef " + dataDefFileParam.fileUrl, e);
                status.setError(true);
                status.setMessage("An error occurred when fetching " + dataDefFileParam.fileUrl + ". " + e.toString());
            }
        }

        attributes.addFlashAttribute("status", status);
        attributes.addFlashAttribute("reindexOptions", dataDefFileParam);
        return "redirect:/admin";
    }

    /**
     * Add a new data definition file, given its URL.
     *
     * @param dataDefFileParam The form parameter specifying the URL of the data definition.
     * @param attributes       Spring MVC attributes for the redirect back to the admin page after reindexing has started.
     */
    @PostMapping("/addDatadef")
    public String addDatadef(@ModelAttribute("datadefParam") DataDefFileParam dataDefFileParam,
                             RedirectAttributes attributes) {
        LOGGER.info("addDatadef: " + dataDefFileParam);

        Status status = new Status();
        status.setOk(true);
        status.setMessage("Added a new data definition source '" + dataDefFileParam.getFileUrl() + "'");

        try {
            indexService.addDatadefFile(dataDefFileParam.getFileUrl());
        } catch (Exception e) {
            LOGGER.error("An exception occurred when pulling datadef " + dataDefFileParam.fileUrl, e);
            status.setError(true);
            status.setMessage("An error occurred when fetching " + dataDefFileParam.fileUrl + ". " + e.toString());
        }


        attributes.addFlashAttribute("status", status);
        return "redirect:/admin";
    }

    /**
     * Removes a data definition from the system. The data associated with this data definition WILL PERSIST.
     * In order to delete the data as well, drop it via the {@link AdminController#removeDatadef(DataDefFileParam, RedirectAttributes)} method.
     *
     * @param dataDefFileParam The form parameter specifying the URL of the data definition.
     * @param attributes       Spring MVC attributes for the redirect back to the admin page after reindexing has started.
     */
    @PostMapping("/deleteddf")
    public String removeDatadef(@ModelAttribute("datadefParam") DataDefFileParam dataDefFileParam,
                                RedirectAttributes attributes) {
        LOGGER.info("deleteDdf: " + dataDefFileParam);
        dataDefFileRepository.delete(new DataDefFile(dataDefFileParam.fileUrl, new ArrayList<>()));

        Status status = new Status();
        status.setOk(true);
        status.setMessage("Removed data definition source '" + dataDefFileParam.getFileUrl() + "'");

        attributes.addFlashAttribute("status", status);
        return "redirect:/admin";
    }

    /**
     * Drop all indexed data associated with the given data definition file.
     *
     * @param dataDefFileParam The form parameter specifying the URL of the data definition.
     * @param attributes       Spring MVC attributes for the redirect back to the admin page after reindexing has started.
     */
    @PostMapping("/dropdata")
    public String dropData(@ModelAttribute("datadefParam") DataDefFileParam dataDefFileParam,
                           RedirectAttributes attributes) {
        LOGGER.info("dropdata: " + dataDefFileParam);
        Status status = new Status();

        try {
            Optional<DataDefFile> dataDefFile = dataDefFileRepository.findById(dataDefFileParam.getFileUrl());

//            List<DataDef> dataDefs = dataDefFetcher.getDataDefsFromUri(dataDefFileParam.getFileUrl());
            for (String dataDefIri : dataDefFile.orElseThrow(() -> new FileNotFoundException("not found: " + dataDefFileParam
                    .getFileUrl())).getDataDefIris()) {
                indexService.dropData(dataDefIri);
            }
            status.setOk(true);
            status.setMessage("Dropped data for definitions in file '" + dataDefFileParam.getFileUrl() + "'");
        } catch (Exception e) {
            LOGGER.error("An exception occurred when pulling datadef " + dataDefFileParam.fileUrl, e);
            status.setError(true);
            status.setMessage("An error occurred when fetching " + dataDefFileParam.fileUrl + ". " + e.toString());
        }

        attributes.addFlashAttribute("status", status);
        return "redirect:/admin";
    }

    /**
     * A representation of a data definition source file and related attributes. The meaning of the object depends on the
     * method that consumes it.
     */
    class DataDefFileParam {
        private String fileUrl = "All";
        private String reindexType = "incremental";
        private boolean reindexAll = false;

        public DataDefFileParam() {
        }

        public boolean isReindexAll() {
            return reindexAll;
        }

        public void setReindexAll(boolean reindexAll) {
            this.reindexAll = reindexAll;
        }

        public String getFileUrl() {
            return fileUrl;
        }

        public void setFileUrl(String fileUrl) {
            this.fileUrl = fileUrl;
        }

        public String getReindexType() {
            return reindexType;
        }

        public void setReindexType(String reindexType) {
            this.reindexType = reindexType;
        }

        @Override
        public String toString() {
            return "DataDefFileParam{" +
                    "fileUrl='" + fileUrl + '\'' +
                    ", reindexType='" + reindexType + '\'' +
                    ", reindexAll=" + reindexAll +
                    '}';
        }
    }

    /**
     * A representation of currently running or stopped jobs in the system. This is processesd by the Thymeleaf template
     * to show the status to the user.
     */
    class IndexingJobsStatus {
        private List<DataDef> runningJobs;
        private List<IndexService.FinishedJobReport> finishedJobs;
        private List<String> errorJobs;

        public IndexingJobsStatus() {
            runningJobs = new ArrayList<>();
            errorJobs = new ArrayList<>();
            finishedJobs = new ArrayList<>();
        }

        public List<DataDef> getRunningJobs() {
            return runningJobs;
        }

        public List<String> getErrorJobs() {
            return errorJobs;
        }

        public void addRunningJob(DataDef dataDef) {
            this.runningJobs.add(dataDef);
        }

        public void addAllRunningJobs(Collection<DataDef> dataDefs) {
            runningJobs.addAll(dataDefs);
        }

        public void addErrorJob(DataDef dataDef, String message) {
            this.errorJobs.add(dataDef.getUri() + " - " + message);
        }

        public List<IndexService.FinishedJobReport> getFinishedJobs() {
            return finishedJobs;
        }

        public void addAllFinishedJobs(Collection<IndexService.FinishedJobReport> finishedJobReports) {
            this.finishedJobs.addAll(finishedJobReports);
        }
    }

    /**
     * A class describing the status of the last operation. For example, if adding a new data definition source fails,
     * the status is set to an error and the appropriate message is shown to the user.
     */
    class Status {
        boolean ok;
        boolean error;
        String message;

        public boolean isOk() {
            return ok;
        }

        public void setOk(boolean ok) {
            this.ok = ok;
            this.error = !ok;
        }

        public boolean isError() {
            return error;
        }

        public void setError(boolean error) {
            this.error = error;
            this.ok = !error;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    /**
     * A single row of the data definition overview table.
     */
    class DataDefFileTableRow {
        private final DataDefFile dataDefFile;
        private final int indexedCount;

        public DataDefFileTableRow(DataDefFile dataDefFile, int indexedCount) {
            this.dataDefFile = dataDefFile;
            this.indexedCount = indexedCount;
        }

        public DataDefFile getDataDefFile() {
            return dataDefFile;
        }

        public int getIndexedCount() {
            return indexedCount;
        }
    }
}
