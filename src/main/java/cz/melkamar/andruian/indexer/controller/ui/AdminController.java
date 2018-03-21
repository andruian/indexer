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

import java.io.IOException;
import java.util.List;

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

    @ModelAttribute
    public void addAttributes(Model model) {
        model.addAttribute("module", "admin");
//        model.addAttribute("datadefs", Arrays.stream(indexerConfiguration.getDataDefUris()).map(DataDefFile::new).collect(Collectors.toList()));
        model.addAttribute("datadefs", dataDefFileRepository.findAll());
        Util.addPrincipalAttribute(model);
    }

    @GetMapping
    public String admin(Model model) {
        if (!model.containsAttribute("datadefParam"))
            model.addAttribute("datadefParam", new DataDefFileParam());


        return "admin";
    }

    @PostMapping("/reindex")
    public String reindex(@ModelAttribute("reindexOptions") DataDefFileParam dataDefFileParam,
                          RedirectAttributes attributes) {
        LOGGER.info("reindex "+dataDefFileParam);

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

    @PostMapping("/addDatadef")
    public String addDatadef(@ModelAttribute("datadefParam") DataDefFileParam dataDefFileParam,
                             RedirectAttributes attributes) {
        LOGGER.info("addDatadef: " + dataDefFileParam);

        Status status = new Status();
        status.setOk(true);
        status.setMessage("Added a new data definition source '" + dataDefFileParam.getFileUrl() + "'");

        // TODO call indexService method to add the datadef to mongo + show existing mongo datadefs + load configuration on start and add it to mongo if not already there
        dataDefFileRepository.insert(new DataDefFile(dataDefFileParam.getFileUrl()));

        attributes.addFlashAttribute("status", status);
        return "redirect:/admin";
    }

    @PostMapping("/deleteddf")
    public String removeDatadef(@ModelAttribute("datadefParam") DataDefFileParam dataDefFileParam,
                                RedirectAttributes attributes) {
        LOGGER.info("deleteDdf: " + dataDefFileParam);
        dataDefFileRepository.delete(new DataDefFile(dataDefFileParam.fileUrl));

        Status status = new Status();
        status.setOk(true);
        status.setMessage("Removed data definition source '" + dataDefFileParam.getFileUrl() + "'");

        attributes.addFlashAttribute("status", status);
        return "redirect:/admin";
    }

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

}
