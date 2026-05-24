package moe.vitamin.campuslink.config.yaml;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class YamlConfig {

    @Getter
    private final File file;
    private Map<String, Object> data;
    private final Yaml yaml;

    /**
     * JAR 파일이 있는 디렉토리를 기준으로 YAML 파일을 생성합니다.
     * @param relativePath JAR 디렉토리 기준 상대 경로
     */
    public static YamlConfig fromJarDirectory(String relativePath) {
        String jarPath = getJarDirectory();
        File file = new File(jarPath, relativePath);
        return new YamlConfig(file);
    }

    /**
     * 현재 실행 중인 JAR 파일의 디렉토리를 반환합니다.
     */
    public static String getJarDirectory() {
        try {
            String classPath = YamlConfig.class.getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI()
                    .getPath();

            if (classPath.endsWith(".jar")) {
                return new File(classPath).getParent();
            } else {
                // IDE에서 실행 중인 경우
                return System.getProperty("user.dir");
            }
        } catch (Exception e) {
            return System.getProperty("user.dir");
        }
    }

    public YamlConfig(File file) {
        this.file = file;
        this.yaml = new Yaml();
        this.data = new HashMap<>();
    }

    public YamlConfig(String filePath) {
        this(new File(filePath));
    }

    /**
     * YAML 파일을 로드합니다.
     * 파일이 없으면 새로운 파일을 생성합니다.
     */
    public void load() {
        if (!file.exists()) {
            this.data = new HashMap<>();
            return;
        }

        try (FileInputStream fis = new FileInputStream(file)) {
            Map<String, Object> loaded = yaml.load(fis);
            this.data = (loaded != null) ? loaded : new HashMap<>();
        } catch (IOException e) {
            log.error("Failed to load yaml file. file={}", file, e);
            this.data = new HashMap<>();
        }
    }

    /**
     * 현재 데이터를 YAML 파일로 저장합니다.
     */
    public void save() {
        try {
            // 디렉토리가 없으면 생성
            file.getParentFile().mkdirs();

            try (FileWriter writer = new FileWriter(file)) {
                yaml.dump(data, writer);
            }
        } catch (IOException e) {
            log.error("Failed to save yaml data to file. file={}, data={}", file, data, e);
        }
    }

    /**
     * 루트 YamlNode를 반환합니다.
     */
    public YamlNode getRoot() {
        return new YamlNode(data, this, "");
    }

    /**
     * 특정 키의 YamlNode를 반환합니다.
     */
    public YamlNode getNode(String key) {
        Object value = data.get(key);

        if (value == null) {
            Map<String, Object> newMap = new HashMap<>();
            data.put(key, newMap);
            return new YamlNode(newMap, this, key);
        }

        if (value instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) value;
            return new YamlNode(map, this, key);
        }

        throw new ClassCastException("Cannot cast " + value.getClass().getSimpleName() + " to Map");
    }

    public Object get(String key) {
        return data.get(key);
    }

    public void set(String key, Object value) {
        data.put(key, value);
    }

    public boolean contains(String key) {
        return data.containsKey(key);
    }

    public void remove(String key) {
        data.remove(key);
    }

    public Map<String, Object> getAll() {
        return new HashMap<>(data);
    }

    public String getFilePath() {
        return file.getAbsolutePath();
    }
}
