package moe.vitamin.campuslink.config.yaml;

import lombok.Getter;

import java.util.*;

public class YamlNode {

    private final Map<String, Object> data;
    private final YamlConfig root;
    /**
     * -- GETTER --
     *  현재 경로를 반환합니다.
     */
    @Getter
    private final String path;

    public YamlNode(Map<String, Object> data, YamlConfig root, String path) {
        this.data = data != null ? data : new HashMap<>();
        this.root = root;
        this.path = path;
    }

    // ============ 기본 GET 메서드 ============

    /**
     * 현재 노드의 전체 Object를 반환합니다.
     */
    public Object get() {
        return data;
    }

    /**
     * 키에 해당하는 값을 Object로 반환합니다.
     */
    public Object get(String key) {
        return data.get(key);
    }

    /**
     * 키에 해당하는 값을 지정된 타입으로 반환합니다 (와일드카드 캐스팅).
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type) {
        Object value = data.get(key);
        if (value == null) return null;
        if (type.isInstance(value)) {
            return (T) value;
        }
        throw new ClassCastException("Cannot cast " + value.getClass().getSimpleName() + " to " + type.getSimpleName());
    }

    /**
     * 키에 해당하는 값을 Object로 반환합니다 (기본값 지원).
     */
    public Object getOrDefault(String key, Object defaultValue) {
        return data.getOrDefault(key, defaultValue);
    }

    /**
     * 키에 해당하는 값을 지정된 타입으로 반환합니다 (기본값 지원).
     */
    @SuppressWarnings("unchecked")
    public <T> T getOrDefault(String key, Class<T> type, T defaultValue) {
        Object value = data.get(key);
        if (value == null) return defaultValue;
        if (type.isInstance(value)) {
            return (T) value;
        }
        throw new ClassCastException("Cannot cast " + value.getClass().getSimpleName() + " to " + type.getSimpleName());
    }

    // ============ String 관련 메서드 ============

    public String getString(String key) {
        Object value = data.get(key);
        return value != null ? value.toString() : null;
    }

    public String getStringOrDefault(String key, String defaultValue) {
        Object value = data.get(key);
        return value != null ? value.toString() : defaultValue;
    }

    // ============ Integer 관련 메서드 ============

    public Integer getInt(String key) {
        Object value = data.get(key);
        if (value == null) return null;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Cannot parse '" + value + "' as Integer");
            }
        }
        if (value instanceof Number) return ((Number) value).intValue();
        throw new ClassCastException("Cannot cast " + value.getClass().getSimpleName() + " to Integer");
    }

    public Integer getIntOrDefault(String key, Integer defaultValue) {
        try {
            Integer value = getInt(key);
            return value != null ? value : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    // ============ Long 관련 메서드 ============

    public Long getLong(String key) {
        Object value = data.get(key);
        if (value == null) return null;
        if (value instanceof Long) return (Long) value;
        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Cannot parse '" + value + "' as Long");
            }
        }
        if (value instanceof Number) return ((Number) value).longValue();
        throw new ClassCastException("Cannot cast " + value.getClass().getSimpleName() + " to Long");
    }

    public Long getLongOrDefault(String key, Long defaultValue) {
        try {
            Long value = getLong(key);
            return value != null ? value : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    // ============ Double 관련 메서드 ============

    public Double getDouble(String key) {
        Object value = data.get(key);
        if (value == null) return null;
        if (value instanceof Double) return (Double) value;
        if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Cannot parse '" + value + "' as Double");
            }
        }
        if (value instanceof Number) return ((Number) value).doubleValue();
        throw new ClassCastException("Cannot cast " + value.getClass().getSimpleName() + " to Double");
    }

    public Double getDoubleOrDefault(String key, Double defaultValue) {
        try {
            Double value = getDouble(key);
            return value != null ? value : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    // ============ Boolean 관련 메서드 ============

    public Boolean getBoolean(String key) {
        Object value = data.get(key);
        if (value == null) return null;
        if (value instanceof Boolean) return (Boolean) value;
        if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        throw new ClassCastException("Cannot cast " + value.getClass().getSimpleName() + " to Boolean");
    }

    public Boolean getBooleanOrDefault(String key, Boolean defaultValue) {
        try {
            Boolean value = getBoolean(key);
            return value != null ? value : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    // ============ List 관련 메서드 ============

    @SuppressWarnings("unchecked")
    public List<Object> getList(String key) {
        Object value = data.get(key);
        if (value == null) return null;
        if (value instanceof List) {
            return (List<Object>) value;
        }
        throw new ClassCastException("Cannot cast " + value.getClass().getSimpleName() + " to List");
    }

    @SuppressWarnings("unchecked")
    public List<Object> getListOrDefault(String key, List<Object> defaultValue) {
        try {
            List<Object> value = getList(key);
            return value != null ? value : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    @SuppressWarnings("unchecked")
    public List<String> getStringList(String key) {
        Object value = data.get(key);
        if (value == null) return null;
        if (value instanceof List) {
            List<Object> list = (List<Object>) value;
            List<String> result = new ArrayList<>();
            for (Object item : list) {
                result.add(item != null ? item.toString() : null);
            }
            return result;
        }
        throw new ClassCastException("Cannot cast " + value.getClass().getSimpleName() + " to List<String>");
    }

    public List<String> getStringListOrDefault(String key, List<String> defaultValue) {
        try {
            List<String> value = getStringList(key);
            return value != null ? value : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    @SuppressWarnings("unchecked")
    public List<Integer> getIntegerList(String key) {
        Object value = data.get(key);
        if (value == null) return null;
        if (value instanceof List) {
            List<Object> list = (List<Object>) value;
            List<Integer> result = new ArrayList<>();
            for (Object item : list) {
                if (item instanceof Integer) {
                    result.add((Integer) item);
                } else if (item instanceof String) {
                    try {
                        result.add(Integer.parseInt((String) item));
                    } catch (NumberFormatException e) {
                        result.add(null);
                    }
                } else if (item instanceof Number) {
                    result.add(((Number) item).intValue());
                }
            }
            return result;
        }
        throw new ClassCastException("Cannot cast " + value.getClass().getSimpleName() + " to List<Integer>");
    }

    public List<Integer> getIntegerListOrDefault(String key, List<Integer> defaultValue) {
        try {
            List<Integer> value = getIntegerList(key);
            return value != null ? value : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    @SuppressWarnings("unchecked")
    public List<Double> getDoubleList(String key) {
        Object value = data.get(key);
        if (value == null) return null;
        if (value instanceof List) {
            List<Object> list = (List<Object>) value;
            List<Double> result = new ArrayList<>();
            for (Object item : list) {
                if (item instanceof Double) {
                    result.add((Double) item);
                } else if (item instanceof String) {
                    try {
                        result.add(Double.parseDouble((String) item));
                    } catch (NumberFormatException e) {
                        result.add(null);
                    }
                } else if (item instanceof Number) {
                    result.add(((Number) item).doubleValue());
                }
            }
            return result;
        }
        throw new ClassCastException("Cannot cast " + value.getClass().getSimpleName() + " to List<Double>");
    }

    public List<Double> getDoubleListOrDefault(String key, List<Double> defaultValue) {
        try {
            List<Double> value = getDoubleList(key);
            return value != null ? value : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    @SuppressWarnings("unchecked")
    public List<Long> getLongList(String key) {
        Object value = data.get(key);
        if (value == null) return null;
        if (value instanceof List) {
            List<Object> list = (List<Object>) value;
            List<Long> result = new ArrayList<>();
            for (Object item : list) {
                if (item instanceof Long) {
                    result.add((Long) item);
                } else if (item instanceof String) {
                    try {
                        result.add(Long.parseLong((String) item));
                    } catch (NumberFormatException e) {
                        result.add(null);
                    }
                } else if (item instanceof Number) {
                    result.add(((Number) item).longValue());
                }
            }
            return result;
        }
        throw new ClassCastException("Cannot cast " + value.getClass().getSimpleName() + " to List<Long>");
    }

    public List<Long> getLongListOrDefault(String key, List<Long> defaultValue) {
        try {
            List<Long> value = getLongList(key);
            return value != null ? value : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    // ============ Nested YamlNode 메서드 ============

    /**
     * 중첩된 YamlNode를 반환합니다.
     */
    @SuppressWarnings("unchecked")
    public YamlNode getNode(String key) {
        Object value = data.get(key);
        if (value == null) {
            return new YamlNode(new HashMap<>(), root, path.isEmpty() ? key : path + "." + key);
        }
        if (value instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) value;
            return new YamlNode(map, root, path.isEmpty() ? key : path + "." + key);
        }
        throw new ClassCastException("Cannot cast " + value.getClass().getSimpleName() + " to Map");
    }

    // ============ SET 메서드 ============

    /**
     * 키-값을 설정합니다.
     */
    public YamlNode set(String key, Object value) {
        data.put(key, value);
        return this;
    }

    /**
     * 여러 키-값을 한 번에 설정합니다.
     */
    public YamlNode setAll(Map<String, Object> map) {
        data.putAll(map);
        return this;
    }

    // ============ SAVE 메서드 ============

    /**
     * 파일에 저장합니다.
     */
    public void save() {
        if (root != null) {
            root.save();
        }
    }

    // ============ 유틸리티 메서드 ============

    /**
     * 키가 존재하는지 확인합니다.
     */
    public boolean contains(String key) {
        return data.containsKey(key);
    }

    /**
     * 키를 삭제합니다.
     */
    public YamlNode remove(String key) {
        data.remove(key);
        return this;
    }

    /**
     * 현재 노드의 모든 키를 반환합니다.
     */
    public Set<String> keys() {
        return data.keySet();
    }

    /**
     * 현재 노드의 크기를 반환합니다.
     */
    public int size() {
        return data.size();
    }

    /**
     * 현재 노드를 초기화합니다.
     */
    public YamlNode clear() {
        data.clear();
        return this;
    }

    /**
     * 현재 노드의 모든 데이터를 복사본으로 반환합니다.
     */
    public Map<String, Object> toMap() {
        return new HashMap<>(data);
    }
}
