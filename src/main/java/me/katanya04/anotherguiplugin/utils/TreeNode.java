package me.katanya04.anotherguiplugin.utils;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TreeNode<T, U extends TreeNode<T, U>> implements Iterable<U> {
    protected T data;
    protected TreeNode<T, U> parent;
    protected final List<TreeNode<T, U>> children;

    public TreeNode(T data) {
        this.data = data;
        this.children = new LinkedList<>();
    }

    public TreeNode(U node) {
        this.data = node.data;
        this.children = new LinkedList<>();
        recursiveCopyChildren(this.cast(), node);
    }

    protected void recursiveCopyChildren(U destination, U sender) {
        destination.children.clear();
        for (TreeNode<T, U> node : sender.children)
            destination.addChild((new TreeNode<>(node.cast())).cast());
    }

    public U addChild(T child) {
        U childNode = new TreeNode<T, U>(child).cast();
        addChild(childNode);
        return this.cast();
    }

    public U addChild(U child) {
        child.parent = this;
        this.children.add(child);
        return this.cast();
    }

    @SafeVarargs
    public final U addChildren(U... child) {
        Arrays.stream(child).forEach(this::addChild);
        return this.cast();
    }

    public final U addChildren(Collection<U> children) {
        children.forEach(this::addChild);
        return this.cast();
    }

    public U setChild(U child, int n) {
        if (n >= children.size())
            throw new ArrayIndexOutOfBoundsException();
        child.parent = this;
        this.children.set(n, child);
        return this.cast();
    }

    @Override
    public Iterator<U> iterator() {
        return getChildren().iterator();
    }

    public List<U> getChildren() {
        return children.stream().map(TreeNode::cast).collect(Collectors.toList());
    }

    public U getChild(int n) {
        return children.get(n).cast();
    }

    public U getFirstChildGivenData(T data) {
        return getChildByPredicate(node -> Objects.equals(node.data, data), false);
    }

    public U getChildByPredicate(Predicate<U> predicate, boolean recursive) {
        U toret = null;
        for (U node : this.getChildren()) {
            if (predicate.test(node))
                return node;
            if (recursive && node.numChildren() > 0)
                toret = node.getChildByPredicate(predicate, true);
            if (toret != null)
                return toret;
        }
        return toret;
    }

    public List<U> getChildrenByPredicate(Predicate<U> predicate, boolean recursive) {
        List<U> toret = new ArrayList<>();
        for (U node : this.getChildren()) {
            if (predicate.test(node))
                toret.add(node);
            if (recursive && node.numChildren() > 0)
                toret.addAll(node.getChildrenByPredicate(predicate, true));
        }
        return toret;
    }

    public void applyToChildren(Consumer<U> function, boolean recursive) {
        for (U node : this.getChildren()) {
            function.accept(node);
            if (recursive && node.numChildren() > 0)
                node.applyToChildren(function, true);
        }
    }

    public List<U> getChildrenGivenData(T data, boolean recursive) {
        return this.getChildrenByPredicate(node -> Objects.equals(node.data, data), recursive);
    }

    public boolean removeChild(U node) {
        return children.remove(node);
    }

    public U getParent() {
        return parent != null ? parent.cast() : null;
    }

    public int numChildren() {
        return children.size();
    }

    public U setValue(T data) {
        this.data = data;
        return this.cast();
    }

    public T getData() {
        return data;
    }

    public U getRoot() {
        TreeNode<T, U> root = this;
        while (root.parent != null)
            root = root.parent;
        return root.cast();
    }

    public String getPath() {
        StringBuilder path = new StringBuilder();
        TreeNode<T, U> aux = this;
        List<String> reversePath = new ArrayList<>();
        while (aux != null) {
            reversePath.add(aux.getId());
            aux = aux.parent;
        }
        ListIterator<String> li = reversePath.listIterator(reversePath.size());
        while(li.hasPrevious()) {
            path.append(li.previous());
            if (li.hasPrevious())
                path.append(".");
        }
        return path.toString();
    }

    public U getNodeFromPath(String path) {
        String[] pathSplit = path.split("\\.");
        TreeNode<T, U> node = this.getRoot();
        if (!node.getId().equals(pathSplit[0]))
            return null;
        for (int i = 1; i < pathSplit.length; i++) {
            node = node.getChildGivenId(pathSplit[i]);
            if (node == null)
                return null;
        }
        return node.cast();
    }

    private U getChildGivenId(String id) {
        return getChildByPredicate(node -> Objects.equals(node.getId(), id), false);
    }

    public String getId() {
        TreeNode<T, U> parent = this.getParent();
        if (parent == null)
            return "0";
        int i = 0;
        for (U child : parent.getChildren()) {
            if (this.equals(child))
                return String.valueOf(i);
            i++;
        }
        return "-1";
    }

    protected U cast() {
        return (U) this;
    }

    @Override
    public String toString() {
        return recursiveToString(0);
    }
    protected String recursiveToString(int indentationLevel) {
        StringBuilder sb = new StringBuilder();
        int i = indentationLevel;
        while (i-- > 0)
            sb.append(" ");
        Class<? extends TreeNode> clazz = this.cast().getClass();
        sb.append(clazz.getSimpleName()).append(" data=").append(getData()).append(" id=").append(getId());
        if (this.numChildren() > 0) {
            sb.append(" children={\n");
            for (U node : this.getChildren()) {
                sb.append(node.recursiveToString(indentationLevel + 1));
                sb.append("\n");
            }
            sb.append("\n");
            i = indentationLevel;
            while (i-- > 0)
                sb.append(" ");
            sb.append("}\n");
        }
        return sb.toString();
    }
    public boolean isLeaf() {
        return this.numChildren() == 0;
    }
}
