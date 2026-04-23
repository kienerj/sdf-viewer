package io.github.kienerj.sdf_reader;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.slf4j.profiler.Profiler;

public class App {

    public static void main(String[] args) throws IOException {

        //rafWrapperTest();

        Profiler profiler = new Profiler("Read Random Records");
        //String filePath = "C:/Users/kienerj/AppData/Local/NoBackup/Apps/Zinc/Zinc Subset 13 sdf/13_p0.0.sdf";
        String filePath = "F:/Dokumente/Programmierung/Zinc/Subset 13 Usual/sdf/13_p0.0.sdf";

        profiler.start("Initialize ScrollingSdfReader");

        SdfReader sdfReader = new SdfReader(filePath);

        profiler.start("Get 1 Record");
        SdfRecord record1 = sdfReader.getRecord(1);
        System.out.println(record1.getMolfileName());
        profiler.start("Get 10 Records at start of file");
        List<SdfRecord> records = sdfReader.getRecords(0, 9);
        profiler.start("Print Records");
        for (SdfRecord record : records) {
            System.out.println(record.getMolfileName());
        }
        profiler.start("Get 10 Records towards end of file");
        records = sdfReader.getRecords(100000, 100009);
        profiler.start("Print Records");
        for (SdfRecord record : records) {
            System.out.println(record.getMolfileName());
        }
        profiler.start("Repeat: Get 10 Records at start of file");
        records = sdfReader.getRecords(0, 9);
        profiler.start("Print Records");
        for (SdfRecord record : records) {
            System.out.println(record.getMolfileName());
        }
        profiler.start("Repeat: Get 10 Records towards end of file");
        records = sdfReader.getRecords(100000, 100009);
        profiler.start("Print Records");
        for (SdfRecord record : records) {
            System.out.println(record.getMolfileName());
        }
        profiler.start("Close Reader");
        sdfReader.close();
        profiler.stop().print();
    }

    public static void saveLoadMap(HashMap<Integer, Long> map) throws IOException {
        //write to file : "fileone"
        Profiler profiler = new Profiler("Serialize - Deserialize HashMap");
        profiler.start("Serialize");
        Properties properties = new Properties();

        for (Map.Entry<Integer, Long> entry : map.entrySet()) {
            properties.put(entry.getKey().toString(), entry.getValue().toString());
        }

        properties.store(new FileOutputStream("index.properties"), null);

        //read from file 
        profiler.start("De-Serialize");
        Map<Integer, Long> index = new HashMap<>();
        Properties propertiesLoaded = new Properties();
        propertiesLoaded.load(Files.newInputStream(Paths.get("index.properties")));

        for (Map.Entry<Object,Object> entry : propertiesLoaded.entrySet()) {            
            index.put(Integer.parseInt((String)entry.getKey()), Long.parseLong((String)entry.getValue()));
        }
        profiler.stop().print();
        System.out.println(index.size());
    }
}
