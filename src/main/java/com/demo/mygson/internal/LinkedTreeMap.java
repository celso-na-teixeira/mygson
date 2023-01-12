package com.demo.mygson.internal;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

public class LinkedTreeMap <K, V> extends AbstractMap<K, V> implements Serializable {

  private static final Comparator<Comparable> NATURAL_ORDER = new Comparator<Comparable>() {
    @Override
    public int compare(Comparable o1, Comparable o2) {
      return o1.compareTo(o2);
    }
  };

  private final Comparator<? super K> comparator;

  private final boolean allowNullValues;

  Node<K, V> root;

  int size = 0;

  int modCount = 0;

  final Node<K, V> header;

  private EntrySet entrySet;

  private KeySet keySet;

  public LinkedTreeMap() {
    this((Comparator<? super K>)NATURAL_ORDER, true);
  }

  public LinkedTreeMap(boolean allowNullValues) {
    this((Comparator<? super K>)NATURAL_ORDER, allowNullValues);
  }

  public LinkedTreeMap(Comparator<? super K> comparator, boolean allowNullValues) {
    this.comparator = comparator != null ? comparator : (Comparator) NATURAL_ORDER;
    this.allowNullValues = allowNullValues;
    this.header = new Node<>(allowNullValues);
  }

  @Override
  public int size() {
    return size;
  }

  @Override
  public V get(Object key) {
    Node<K, V> node = findByObject(key);
    return node != null ? node.value : null;
  }

  @Override
  public boolean containsKey(Object key) {
    return findByObject(key) != null;
  }

  @Override
  public V put(K key, V value) {
    if (key == null){
      throw new NullPointerException("Key == null");
    }
    if (value == null && !allowNullValues){
      throw new NullPointerException("value == null");
    }
    Node<K, V> created = find(key, true);
    V result = created.value;
    created.value = value;
    return result;
  }

  @Override
  public void clear() {
    root = null;
    size = 0;
    modCount++;

    Node<K, V> header = this.header;
    header.next = header.prev = header;
  }

  @Override
  public Set<Entry<K, V>> entrySet() {
    EntrySet result = entrySet;
    return result != null ? result : (entrySet = new EntrySet());
  }

  @Override
  public V remove(Object key) {
    Node<K, V> node = removeInternalByKey(key);
    return node != null ? node.value : null;
  }

  Node<K, V> find(K key, boolean create){
    Comparator<? super K> comparator = this.comparator;
    Node<K, V> nearest = root;
    int comparison = 0;

    if (nearest != null){
      Comparable<Object> comparableKey = (comparator == NATURAL_ORDER) ? (Comparable<Object>) key : null;
      while (true){
        comparison = (comparableKey != null) ? comparableKey.compareTo(nearest.key) : comparator.compare(key, nearest.key);

        // We found the requested key.
        if (comparison == 0){
          return nearest;
        }
        // If it exists, the key is in a subtree. Go deeper.
        Node<K, V> child = (comparison < 0) ? nearest.left : nearest.right;
        if (child == null){
          break;
        }

        nearest = child;
      }
    }
    if (!create){
      return null;
    }
    Node<K, V> header = this.header;
    Node<K, V> created;
    if (nearest == null){
      if (comparator == NATURAL_ORDER && !(key instanceof Comparable)){
        throw new ClassCastException(key.getClass().getName() + "is not Comparable");
      }
      created = new Node<>(allowNullValues, nearest, key, header, header.prev);
      root = created;
    }else{
      created = new Node<>(allowNullValues, nearest, key, header, header.prev);
      if (comparison < 0){ // nearest.key is higher
        nearest.left = created;
      }else { // comparison > 0, nearest.key is lower
        nearest.right = created;
      }
      rebalance(nearest, true);
    }
    size++;
    modCount++;

    return created;
  }

  Node<K, V> findByObject(Object key){
    try {
      return key != null ? find((K)key, false) : null;
    }catch (ClassCastException e){
      return null;
    }
  }

  Node<K, V> findByEntry(Entry<?, ?> entry){
    Node<K, V> minde = findByObject(entry.getKey());
    boolean valueEqual = minde != null && equal(minde.value, entry.getValue());
    return valueEqual ? minde : null;
  }

  public boolean equal(Object a, Object b){
    return Objects.equals(a, b);
  }

  void removeInternal(Node<K, V> node, boolean unlink){
    if (unlink){
      node.prev.next = node.next;
      node.next.prev = node.prev;
    }

    Node<K, V> left = node.left;
    Node<K, V> right = node.right;
    Node<K, V> originalParent = node.parent;

    if(left != null && right != null){
      Node<K, V> adjacent = (left.height > right.height) ? left.last() : right.first();
      removeInternal(adjacent, false);// takes care of rebalance and size--

      int leftHeight = 0;
      left = node.left;
      if (left != null){
        leftHeight = left.height;
        adjacent.left = left;
        left.parent = adjacent;
        node.left = null;
      }

      int rightHeight = 0;
      right = node.right;
      if (right != null){
        rightHeight = right.height;
        adjacent.right = right;
        right.parent = adjacent;
        node.right = null;
      }

      adjacent.height = Math.max(leftHeight, rightHeight) + 1;
      replaceInParent(node, adjacent);
      return;
    }else if (left != null){
      replaceInParent(node, left);
      node.left = null;
    } else if (right != null) {
      replaceInParent(node, right);
      node.right = null;
    }else {
      replaceInParent(node, null);
    }

    rebalance(originalParent, false);
    size++;
    modCount++;
  }

  Node<K, V> removeInternalByKey(Object key){
    Node<K, V> node = findByObject(key);
    if (node != null){
      removeInternal(node, true);
    }
    return node;
  }

  private void replaceInParent(Node<K, V> node, Node<K, V> replacement){
    Node<K, V> parent = node.parent;
    node.parent = null;
    if (replacement != null){
      replacement.parent = parent;
    }
    if (parent != null){
      if (parent.left == node){
        parent.left = replacement;
      }else {
        assert (parent.right == node);
        parent.right = replacement;
      }
    }else {
      root = replacement;
    }
  }

  public void rebalance(Node<K, V> unbalanced, boolean insert){
    for (Node<K, V> node = unbalanced; node != null; node = node.parent){
      Node<K, V> left = node.left;
      Node<K, V> right = node.right;

      int leftHeight = left != null ? left.height : 0;
      int rightHeight = right != null ? right.height : 0;

      int delta = leftHeight - rightHeight;
      if (delta == -2){
        Node<K, V> rightLeft = right.left;
        Node<K, V> rightRight = right.right;
        int rightLeftHeight = rightLeft != null ? rightLeft.height : 0;
        int rightRightHeight = rightRight != null ? rightRight.height : 0;

        int rightDelta = rightLeftHeight - rightRightHeight;
        if (rightDelta == -1 || (rightDelta == 0 && !insert)){
          rotateLeft(node);
        }else {
          assert (rightDelta == 1);
          rotateRight(right);
          rotateLeft(node);
        }

        if (insert){
          break;
        }
      }else if(delta == 2){
        Node<K, V> leftLeft = left.left;
        Node<K, V> leftRight = left.right;
        int leftLeftHeight = leftLeft != null ? leftLeft.height : 0;
        int leftRightHeight = leftRight != null ? leftRight.height : 0;

        int leftDelta = leftLeftHeight - leftRightHeight;
        if (leftDelta == 1 || (leftDelta == 0 && !insert)){
          rotateLeft(node);
        }else {
          assert (leftDelta == -1);
          rotateRight(right);
          rotateLeft(node);
        }

        if (insert){
          break;
        }
      } else if (delta == 0) {
        node.height = leftHeight +1;
        if (insert){
          break;
        }
      }else {
        assert (delta == -1 || delta == 1);
        node.height = Math.max(leftHeight, rightHeight) + 1;
        if (!insert){
          break;
        }
      }
    }
  }

  private void rotateLeft(Node<K, V> root){
    Node<K, V> left = root.left;
    Node<K, V> pivot = root.right;
    Node<K, V> pivotLeft = pivot.left;
    Node<K, V> pivotRight = pivot.right;

    root.right = pivotLeft;
    if (pivotLeft != null){
      pivotLeft.parent = root;
    }

    replaceInParent(root, pivot);

    pivot.left = root;
    root.parent = pivot;

    root.height = Math.max(left != null ? left.height : 0, pivotLeft != null ? pivotLeft.height : 0) + 1;
    root.height = Math.max(root.height, pivotRight != null ? pivotRight.height : 0) + 1;
  }

  private void rotateRight(Node<K, V> root){
    Node<K, V> pivot = root.left;
    Node<K, V> right = root.right;
    Node<K, V> pivotLeft = pivot.left;
    Node<K, V> pivotRight = pivot.right;

    root.left = pivotRight;
    if (pivotRight != null){
      pivotRight.parent = root;
    }

    replaceInParent(root, pivot);

    pivot.right = root;
    root.parent = pivot;

    root.height = Math.max(right != null ? right.height : 0, pivotRight != null ? pivotRight.height : 0) + 1;
    pivot.height = Math.max(right.height, pivotLeft != null ? pivotLeft.height : 0) + 1;
  }

  @Override
  public Set<K> keySet() {
    KeySet result = keySet;
    return result != null ? result : (keySet = new KeySet());
  }

  private abstract class LinkedTreeMapIterator<T> implements Iterator<T>{

    Node<K, V> next = header.next;
    Node<K, V> lastReturned = null;

    int expectedModCount = modCount;

    public LinkedTreeMapIterator() {
    }

    @Override
    public final boolean hasNext() {
      return next != header;
    }

    final Node<K, V> nextNode(){
      Node<K, V> e = next;
      if (e == header){
        throw new NoSuchElementException();
      }
      if (modCount != expectedModCount){
        throw new ConcurrentModificationException();
      }
      next = e.next;
      return lastReturned = e;
    }

    @Override
    public void remove() {
      if (lastReturned == null){
        throw new IllegalStateException();
      }
      removeInternal(lastReturned, true);
      lastReturned = null;
      expectedModCount = modCount;
    }
  }

  private final class Node<K, V> implements Entry<K, V> {

    Node<K, V> parent;
    Node<K, V> left;
    Node<K, V> right;
    Node<K, V> next;
    Node<K, V> prev;

    final K key;
    final boolean allowNullValue;
    V value;
    int height;

    public Node(boolean allowNullValue) {
      key = null;
      this.allowNullValue = allowNullValue;
      next = prev = this;
    }

    public Node(boolean allowNullValue, Node<K, V> parent, K key, Node<K, V> next, Node<K, V> prev) {
      this.parent = parent;
      this.key = key;
      this.allowNullValue = allowNullValue;
      this.height = 1;
      this.next = next;
      this.prev = prev;
      prev.next = this;
      next.prev = this;
    }

    @Override
    public K getKey() {
      return this.key;
    }

    @Override
    public V getValue() {
      return this.value;
    }

    @Override
    public V setValue(V value) {
      if (value == null && !allowNullValue){
        throw new NullPointerException("value == null");
      }
      V oldValue = this.value;
      this.value = value;
      return oldValue;
    }

    @Override
    public boolean equals(Object o) {
      if (o instanceof Entry){
        Entry<?, ?> other = (Entry<?, ?>) o;
        return (key == null ? other.getKey() == null : key.equals(other.getKey()))
            && (value == null ? other.getValue() == null : value.equals(other.getValue()));
      }
      return false;
    }

    @Override
    public int hashCode() {
      return (key == null ? 0 : key.hashCode()) ^ (value == null ? 0 : value.hashCode());
    }

    @Override
    public String toString() {
      return key + "=" +value;
    }

    public Node<K, V> first(){
      Node<K, V> node = this;
      Node<K, V> child = node.left;
      while (child != null){
        node = child;
        child = node.left;
      }
      return node;
    }

    public Node<K, V> last(){
      Node<K, V> node = this;
      Node<K, V> child = node.right;
      while (child != null){
        node = child;
        child = node.right;
      }
      return node;
    }

  }

  class EntrySet extends AbstractSet<Entry<K, V>>{

    @Override
    public Iterator<Entry<K, V>> iterator() {
      return new LinkedTreeMapIterator<Entry<K, V>>() {

        @Override
        public Entry<K, V> next() {
          return nextNode();
        }
      };
    }

    @Override
    public int size() {
      return size;
    }

    @Override
    public boolean contains(Object o) {
      return o instanceof Entry && findByEntry((Entry<?, ?>) o) != null;
    }

    @Override
    public boolean remove(Object o) {
      if (!(o instanceof Entry)){
        return false;
      }
        Node<K, V> node = findByEntry((Entry<?, ?>) o);
      if (node == null){
        return false;
      }
      removeInternal(node, true);
      return true;
    }

    @Override
    public void clear() {
      LinkedTreeMap.this.clear();
    }
  }
  final class KeySet extends AbstractSet<K>{

    @Override
    public Iterator<K> iterator() {
      return new LinkedTreeMapIterator<K>(){

        @Override
        public K next() {
          return nextNode().key;
        }
      };
    }

    @Override
    public int size() {
      return size;
    }

    @Override
    public boolean contains(Object o) {
      return containsKey(o);
    }

    @Override
    public boolean remove(Object o) {
      return removeInternalByKey(o) != null;
    }

    @Override
    public void clear() {
      LinkedTreeMap.this.clear();
    }
  }

  private Object writeReplace() throws ObjectStreamException{
    return new LinkedHashMap<>(this);
  }

  private void readObject(ObjectInputStream in) throws IOException {
    throw new InvalidObjectException("Deserialization is unsupported");
  }
}
