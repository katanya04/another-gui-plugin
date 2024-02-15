package me.katanya04.anotherguiplugin.Utils;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class TreeNode<T, U extends TreeNode<T, U>> implements Iterable<TreeNode<T, U>> {
    protected T data;
    protected TreeNode<T, U> parent;
    protected final List<TreeNode<T, U>> children;

    public TreeNode(T data) {
        this.data = data;
        this.children = new LinkedList<>();
    }

    public TreeNode<T, U> addChild(T child) {
        TreeNode<T, U> childNode = new TreeNode<>(child);
        addChild(childNode);
        return childNode;
    }

    public void addChild(TreeNode<T, U> child) {
        child.parent = this;
        this.children.add(child);
    }

    public void setChild(TreeNode<T, U> child, int n) {
        if (n >= children.size())
            throw new ArrayIndexOutOfBoundsException();
        child.parent = this;
        this.children.set(n, child);
    }

    @Override
    public Iterator<TreeNode<T, U>> iterator() {
        return children.stream().iterator();
    }

    public List<TreeNode<T, U>> getChildren() {
        return children;
    }

    public TreeNode<T, U> getChild(int n) {
        return children.get(n);
    }

    public TreeNode<T, U> getParent() {
        return parent;
    }

    public int numChildren() {
        return children.size();
    }

    public void setValue(T data) {
        this.data = data;
    }

    public T getData() {
        return data;
    }

    public U cast() {
        return (U) this;
    }
}
