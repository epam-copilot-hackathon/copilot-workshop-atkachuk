package com.microsoft.hackathon.copilotdemo.controller;

import java.util.List;

public class FolderContentResponse {
    private List<String> files;
    private List<String> folders;

    public List<String> getFiles() {
        return files;
    }

    public void setFiles(List<String> files) {
        this.files = files;
    }

    public List<String> getFolders() {
        return folders;
    }

    public void setFolders(List<String> folders) {
        this.folders = folders;
    }
}