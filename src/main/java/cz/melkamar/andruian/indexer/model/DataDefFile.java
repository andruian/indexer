package cz.melkamar.andruian.indexer.model;

import org.springframework.data.annotation.Id;

import java.util.List;

public class DataDefFile {
    @Id
    private String fileUrl;
    private List<String> dataDefIris;

    public DataDefFile(String fileUrl, List<String> dataDefIris) {
        this.fileUrl = fileUrl;
        this.dataDefIris = dataDefIris;
    }

    public DataDefFile() {
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public List<String> getDataDefIris() {
        return dataDefIris;
    }

    public void setDataDefIris(List<String> dataDefIris) {
        this.dataDefIris = dataDefIris;
    }

    @Override
    public String toString() {
        return "DataDefFile{" +
                "fileUrl='" + fileUrl + '\'' +
                '}';
    }
}
