package me.katanya04.anotherguiplugin.utils;

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
    public void copy(TreeNode<T, U> node) {
        this.data = node.data;
        recursiveCopyChildren(this, node);
    }

    protected void recursiveCopyChildren(TreeNode<T, U> destination, TreeNode<T, U> sender) {
        int i = 0;
        for (TreeNode<T, U> node : sender.children) {
            if (destination.numChildren() <= i)
                destination.addChild(new TreeNode<>(node.getData()));
            destination.getChild(i).copy(node);
            i++;
        }
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

    public TreeNode<T, U> getChildGivenData(T data) {
        List<TreeNode<T, U>> children = getChildren();
        for (TreeNode<T, U> node : children)
            if (data.equals(node.data))
                return node;
        return null;
    }

    public boolean removeChild(TreeNode<T, U> node) {
        return children.remove(node);
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
