import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FolderData implements Serializable {
    private static final long serialVersionUID = 1L; // Ajout pour compatibilité

    private String name;
    private List<FolderData> subFolders;
    private List<String> files;

    public FolderData(String name) {
        this.name = name;
        this.subFolders = new ArrayList<>();
        this.files = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public List<FolderData> getSubFolders() {
        return subFolders;
    }

    public List<String> getFiles() {
        return files;
    }

    public void addSubFolder(FolderData folder) {
        subFolders.add(folder);
    }

    public void addFile(String fileName) {
        files.add(fileName);
    }
}
