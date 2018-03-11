package cz.melkamar.andruian.indexer.controller.ui;

import cz.melkamar.andruian.indexer.config.IndexerConfiguration;
import cz.melkamar.andruian.indexer.controller.Util;
import cz.melkamar.andruian.indexer.exception.DataDefFormatException;
import cz.melkamar.andruian.indexer.exception.RdfFormatException;
import cz.melkamar.andruian.indexer.model.datadef.DataDef;
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

@Controller
@RequestMapping("/admin")
public class AdminController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AdminController.class);
    private final IndexerConfiguration indexerConfiguration;
    private final IndexService indexService;
    private final DataDefFetcher dataDefFetcher;

    @Autowired
    public AdminController(IndexerConfiguration indexerConfiguration,
                           IndexService indexService, DataDefFetcher dataDefFetcher) {
        this.indexerConfiguration = indexerConfiguration;
        this.indexService = indexService;
        this.dataDefFetcher = dataDefFetcher;
    }

    @ModelAttribute
    public void addAttributes(Model model) {
        model.addAttribute("module", "admin");
        model.addAttribute("datadefs", indexerConfiguration.getDataDefUris());
        Util.addPrincipalAttribute(model);
    }

    @GetMapping
    public String admin(Model model) {
        if (!model.containsAttribute("reindexOptions"))
            model.addAttribute("reindexOptions", new ReindexOptions());

        return "admin";
    }

    @PostMapping("/reindex")
    public String reindex(@ModelAttribute("reindexOptions") ReindexOptions reindexOptions,
                          RedirectAttributes attributes) {
        Status status = new Status();
        if (reindexOptions.datadef.toLowerCase().equals("all")) {
            status.setOk(true);
            status.setMessage(StringUtils.capitalize(reindexOptions.reindexType) + " reindexing started for all data sources.");
            indexService.reindexAll(reindexOptions.reindexType.equals("full"));
        } else {
            try {
                DataDef dataDef = dataDefFetcher.getDataDefFromUri(reindexOptions.datadef);
                status.setOk(true);
                status.setMessage(StringUtils.capitalize(reindexOptions.reindexType) + " reindexing started for " + reindexOptions.datadef);
                indexService.indexDataDef(dataDef, reindexOptions.reindexType.equals("full"));
            } catch (RdfFormatException | DataDefFormatException e) {
                LOGGER.error("An exception occurred when pulling datadef " + reindexOptions.datadef, e);
                status.setError(true);
                status.setMessage("An error occurred when fetching " + reindexOptions.datadef + ". " + e.toString());
            }
        }

        attributes.addFlashAttribute("status", status);
        attributes.addFlashAttribute("reindexOptions", reindexOptions);
        return "redirect:/admin";
    }

    class ReindexOptions {
        String datadef = "All";
        String reindexType = "incremental";

        public ReindexOptions() {
        }

        public String getDatadef() {
            return datadef;
        }

        public void setDatadef(String datadef) {
            this.datadef = datadef;
        }


        public String getReindexType() {
            return reindexType;
        }

        public void setReindexType(String reindexType) {
            this.reindexType = reindexType;
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
