package com.daren.chen.iot.mqtt.api.server.path;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * @Description:
 * @author: chendaren
 * @CreateDate: 2020/7/31 16:57
 */
public class TopicFilter<V> {

    /**
     *
     */
    private final Map<String, List<V>> dataMap = new ConcurrentHashMap<>();
    /**
     * + 只能是单个 不能包含 /
     */
    private static final String KEY_1 = "/+/";
    /**
     * # 只能是最后
     */
    private static final String KEY_2 = "/#";

    /**
     *
     * @param topicName
     */
    private List<String> getKeyOfParseTopicName(String topicName) {
        List<String> list = new ArrayList<>();
        Set<String> ks = dataMap.keySet();
        if (topicName == null || "".equals(topicName) || ks.size() == 0) {
            return list;
        }
        Optional.of(topicName).ifPresent(s -> {
            for (String k : ks) {
                // 包含 #
                if (k.endsWith(KEY_2)) {
                    String substring = k.substring(0, k.indexOf(KEY_2) + 1);
                    if (topicName.startsWith(substring)) {
                        list.add(k);
                    }
                } else if (k.contains(KEY_1)) {
                    String substring1 = k.substring(0, k.indexOf(KEY_1) + 1);
                    String substring2 = k.substring(k.indexOf(KEY_1) + KEY_1.length() - 1);
                    if (topicName.startsWith(substring1) && topicName.endsWith(substring2)) {
                        String substring3 = topicName.substring(topicName.indexOf(substring1) + substring1.length(),
                            topicName.indexOf(substring2));
                        if (!substring3.contains("/")) {
                            list.add(k);
                        }
                    }
                } else {
                    if (k.equals(topicName)) {
                        list.add(k);
                    }
                }
            }

        });

        return list;
    }

    /**
     *
     * @param topicName
     */
    private List<String> getKeyOfFormatTopicName(String topicName) {
        List<String> list = new ArrayList<>();
        Set<String> ks = dataMap.keySet();
        if (topicName == null || "".equals(topicName) || ks.size() == 0) {
            return list;
        }
        Optional.of(topicName).ifPresent(s -> {
            // 包含 #
            if (topicName.endsWith(KEY_2)) {
                String substring = topicName.substring(0, topicName.indexOf(KEY_2) + 1);
                for (String k : ks) {
                    if (k.startsWith(substring)) {
                        list.add(k);
                    }
                }
            } else if (topicName.contains(KEY_1)) {
                String substring1 = topicName.substring(0, topicName.indexOf(KEY_1) + 1);
                String substring2 = topicName.substring(topicName.indexOf(KEY_1) + KEY_1.length() - 1);
                for (String k : ks) {
                    if (k.startsWith(substring1) && k.endsWith(substring2)) {
                        String substring3 =
                            k.substring(k.indexOf(substring1) + substring1.length(), k.indexOf(substring2));
                        if (!substring3.contains("/")) {
                            list.add(k);
                        }
                    }
                }

            } else {
                list.addAll(getKeyOfParseTopicName(topicName));
            }

        });

        return list;
    }

    /**
     *
     * @param topicName
     * @param v
     */
    public void addTopic(String topicName, V v) {
        dataMap.computeIfAbsent(topicName, k -> new CopyOnWriteArrayList<>()).add(v);
    }

    public List<V> getData(String topicName) {
        List<V> list = new ArrayList<>();
        List<String> strings = getKeyOfFormatTopicName(topicName);
        if (strings.size() > 0) {
            for (String string : strings) {
                List<V> list1 = dataMap.get(string);
                if (list1 == null || list1.size() == 0) {
                    continue;
                }
                list.addAll(list1);
            }
        }
        return list.stream().distinct().collect(Collectors.toList());
    }

    /**
     *
     * @param topicName
     * @param v
     * @return
     */
    public List<String> delete(String topicName, V v) {
        if (topicName == null || "".equals(topicName)) {
            return new ArrayList<>();
        }
        List<String> strings = getKeyOfFormatTopicName(topicName);
        if (strings.size() > 0) {
            for (String string : strings) {
                List<V> list = dataMap.get(string);
                if (list != null) {
                    List<V> objects = new CopyOnWriteArrayList<>();
                    for (V v1 : list) {
                        if (v1 != v) {
                            objects.add(v1);
                        }
                    }
                    dataMap.put(string, objects);
                }
            }
            return strings;
        }
        return new ArrayList<>();
    }
}
