package com.daren.chen.iot.mqtt.api.server.path;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import lombok.extern.slf4j.Slf4j;

/**
 * 缓存操作
 *
 * @author chendaren
 * @create 2019-12-02 13:48
 **/
@Slf4j
public class TopicMap<K, V> {

    private final Map<K, Node<K, V>> datas = new ConcurrentHashMap<>();

    /**
     *
     * @param topic
     * @param v
     * @return
     */
    public boolean putData(K[] topic, V v) {
        if (topic == null || topic.length == 0) {
            return false;
        } else if (topic.length == 1) {
            Node<K, V> kvNode = buildOne(topic[0], v);
            if (kvNode != null && kvNode.topic.equals(topic[0])) {
                return true;
            }
        } else {
            Node<K, V> kvNode = buildOne(topic[0], null);
            for (int i = 1; i < topic.length; i++) {
                if (i == 1) {
                    kvNode = kvNode.putNextValue(topic[i], v);
                } else {
                    kvNode = kvNode.putNextValue(topic[i], v);
                }
            }
        }
        return true;
    }

    /**
     *
     * @param ks
     * @param v
     * @return
     */
    public boolean delete(K[] ks, V v) {
        if (ks.length == 1) {
            Node<K, V> kvNode = datas.get(ks[0]);
            return kvNode != null && kvNode.delValue(v);
        } else {
            Node<K, V> kvNode = datas.get(ks[0]);
            for (int i = 1; i < ks.length && kvNode != null; i++) {
                kvNode = kvNode.getNext(ks[i]);
            }
            if (kvNode == null) {
                return false;
            }
            return kvNode.delValue(v);

        }
    }

    /**
     *
     * @param ks
     * @return
     */
    public List<V> getData(K[] ks) {
        if (ks == null || ks.length == 0) {
            return null;
        } else if (ks.length == 1) {
            return datas.get(ks[0]).get();
        } else {
            Node<K, V> node = datas.get(ks[0]);
            if (node != null) {
                List<V> all = new ArrayList<>(node.get());
                for (int i = 1; i < ks.length; i++) {
                    node = node.getNext(ks[i]);
                    if (node == null) {
                        break;
                    }
                    all.addAll(node.get());
                }
                return all;
            }
            return null;
        }
    }

    public Node<K, V> buildOne(K k, V v) {

        Node<K, V> node = this.datas.computeIfAbsent(k, key -> new Node<>(k));
        if (v != null) {
            node.put(v);
        }
        return node;
    }

    static class Node<K, V> {

        private final K topic;

        private final Map<K, Node<K, V>> map = new ConcurrentHashMap<>();

        List<V> vs = new CopyOnWriteArrayList<>();

        public K getTopic() {
            return topic;
        }

        Node(K topic) {
            this.topic = topic;
        }

        public boolean delValue(V v) {
            return vs.remove(v);
        }

        public Node<K, V> putNextValue(K k, V v) {
            Node<K, V> kvNode = map.computeIfAbsent(k, key -> new Node<K, V>(k));
            if (v != null) {
                kvNode.put(v);
            }
            return kvNode;
        }

        public Node<K, V> getNext(K k) {
            return map.get(k);
        }

        public boolean put(V v) {
            return vs.add(v);
        }

        public List<V> get() {
            return vs;
        }
    }
}
