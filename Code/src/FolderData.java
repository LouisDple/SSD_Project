
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


// this is the code that implements a classe for the data of a file that helps with working with recusrive methods for download and upload of folders
public class FolderData implements Serializable {
    private static final long serialVersionUID = 1L; 

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
