package cz.melkamar.andruian.indexer.model;

import org.springframework.data.annotation.Id;

public class DataDefFile {
    @Id
    private String fileUrl;

    public DataDefFile(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public DataDefFile() {
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }



    @Override
    public String toString() {
        return "DataDefFile{" +
                "fileUrl='" + fileUrl + '\'' +
                '}';
    }
}
