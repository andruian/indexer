package cz.melkamar.andruian.indexer.controller.ui;

import cz.melkamar.andruian.indexer.config.IndexerConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/config")
public class ConfigController {


    private final IndexerConfiguration indexerConfiguration;

    @Autowired
    public ConfigController(IndexerConfiguration indexerConfiguration) {
        this.indexerConfiguration = indexerConfiguration;
    }

    @GetMapping("")
    public String config(Model model) {
        Config configModel = new Config();
        configModel.setDatadefs(indexerConfiguration.getDataDefUris());

        model.addAttribute("config", configModel);
        model.addAttribute("module", "config");
        return "config";
    }

    class Config {
        String[] datadefs;

        public String[] getDatadefs() {
            return datadefs;
        }

        public void setDatadefs(String[] datadefs) {
            this.datadefs = datadefs;
        }
    }
}
