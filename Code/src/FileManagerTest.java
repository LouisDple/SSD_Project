

import java.io.IOException;

class FileManagerTest {

    public void testListDirectory() throws IOException {
        FileManager fm = new FileManager("C:\\Users\\Depelley Louis\\Desktop\\courscarta\\ssd\\SSD_Project\\SSD_Project");
        try {
            fm.createDirectory("Code\\src\\testDir\\dir1");
           /*  List<Path> contents = fm.listDirectory("testDir");
            for(Path p : contents){
                System.out.println(p.getFileName());
            }*/
            fm.readTextFile2("C:\\Users\\Depelley Louis\\Desktop\\courscarta\\ssd\\SSD_Project\\SSD_Project\\Code\\src\\testDir\\12.txt");
            fm.copiar("C:\\\\Users\\\\Depelley Louis\\\\Desktop\\\\courscarta\\\\ssd\\\\SSD_Project\\\\SSD_Project\\\\Code\\\\src\\\\testDir\\\\12.txt", "C:\\Users\\Depelley Louis\\Desktop\\courscarta\\ssd\\SSD_Project\\SSD_Project\\Code\\src\\testDir\\dir1\\copie.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) throws IOException {
        new FileManagerTest().testListDirectory();
    }
}