package be.iminds.ilabt.jfed.util;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.util.*;

/**
 * JFedJavaFXBindings
 */
public class JFedJavaFXBindings {
    private JFedJavaFXBindings() {}

    /** order is not important! */
    public static <T> ObservableList<T> union(ObservableList<T> ... args) {
        final ObservableList<T> res = FXCollections.observableArrayList();

        //add initial list
        for (int i = 0 ; i < args.length; i++) {
            ObservableList<T> arg = args[i];
            for (T t : arg)
                res.add(t);
        }

        //update on changes
        ListChangeListener<T> changeListener = new ListChangeListener<T>() {
            @Override
            public void onChanged(Change<? extends T> change) {
                while (change.next()) {
                    if (change.wasAdded())
                        for (T a : change.getAddedSubList()) {
                            res.add(a);
                        }
                    if (change.wasRemoved())
                        for (T d : change.getRemoved()) {
                            res.remove(d);
                        }
                }
            }
        };

        for (int i = 0 ; i < args.length; i++) {
            ObservableList<T> arg = args[i];
            arg.addListener(changeListener);
        }

        return res;
    }




    //this is not a good solution, as listchangeListeners must be in same thread as changes to list :-/
    //see this warningListChangeListener.Change:
//            Warning: This class directly accesses the source list to acquire information about the changes.
//           This effectively makes the Change object invalid when another change occurs on the list.
//           For this reason it is not safe to use this class on a different thread.

//    private static class AssureJavaFXThreadForListenersWrapper<T> implements ObservableList<T> {
//        private ObservableList<T> o;
//        private AssureJavaFXThreadForListenersWrapper(ObservableList<T> o) {
//            this.o = o;
//        }
//
//        private static class MychangeListenerWrapper<T> implements ListChangeListener<T> {
//            private ListChangeListener<? super T> orig;
//            public MychangeListenerWrapper(ListChangeListener<? super T> orig) {
//                this.orig = orig;
//            }
//
//            @Override
//            public void onChanged(final Change<? extends T> change) {
//                if (!Platform.isFxApplicationThread())
//                    Platform.runLater(new Runnable() {
//                        @Override
//                        public void run() {
//                            orig.onChanged(change);
//                        }
//                    });
//                else
//                    orig.onChanged(change);
//            }
//        }
//
//        private Map<ListChangeListener<? super T>, MychangeListenerWrapper<? super T>> origToWrapper = new HashMap();
//
//        @Override
//        public void addListener(ListChangeListener<? super T> listChangeListener) {
//            MychangeListenerWrapper wrapper = new MychangeListenerWrapper(listChangeListener);
//            origToWrapper.put(listChangeListener, wrapper);
//            o.addListener(wrapper);
//        }
//
//        @Override
//        public void removeListener(ListChangeListener<? super T> listChangeListener) {
//            MychangeListenerWrapper wrapper = origToWrapper.remove(listChangeListener);
//            if (wrapper == null) return;
//            o.removeListener(wrapper);
//        }
//
//        @Override
//        public boolean addAll(T... ts) {
//            return o.addAll(ts);
//        }
//
//        @Override
//        public boolean setAll(T... ts) {
//            return o.setAll(ts);
//        }
//
//        @Override
//        public boolean setAll(Collection<? extends T> ts) {
//            return o.setAll(ts);
//        }
//
//        @Override
//        public boolean removeAll(T... ts) {
//            return o.removeAll(ts);
//        }
//
//        @Override
//        public boolean retainAll(T... ts) {
//            return o.retainAll(ts);
//        }
//
//        @Override
//        public void remove(int i, int i1) {
//            o.remove(i, i1);
//        }
//
//        @Override
//        public int size() {
//            return o.size();
//        }
//
//        @Override
//        public boolean isEmpty() {
//            return o.isEmpty();
//        }
//
//        @Override
//        public boolean contains(Object o) {
//            return this.o.contains(o);
//        }
//
//        @Override
//        public Iterator<T> iterator() {
//            return o.iterator();
//        }
//
//        @Override
//        public Object[] toArray() {
//            return o.toArray();
//        }
//
//        @Override
//        public <T> T[] toArray(T[] a) {
//            return o.toArray(a);
//        }
//
//        @Override
//        public boolean add(T t) {
//            return o.add(t);
//        }
//
//        @Override
//        public boolean remove(Object o) {
//            return this.o.remove(o);
//        }
//
//        @Override
//        public boolean containsAll(Collection<?> c) {
//            return o.containsAll(c);
//        }
//
//        @Override
//        public boolean addAll(Collection<? extends T> c) {
//            return o.addAll(c);
//        }
//
//        @Override
//        public boolean addAll(int index, Collection<? extends T> c) {
//            return o.addAll(c);
//        }
//
//        @Override
//        public boolean removeAll(Collection<?> c) {
//            return o.removeAll(c);
//        }
//
//        @Override
//        public boolean retainAll(Collection<?> c) {
//            return o.retainAll(c);
//        }
//
//        @Override
//        public void clear() {
//            o.clear();
//        }
//
//        @Override
//        public boolean equals(Object o) {
//            return this.o.equals(o);
//        }
//
//        @Override
//        public int hashCode() {
//            return o.hashCode();
//        }
//
//        @Override
//        public T get(int index) {
//            return o.get(index);
//        }
//
//        @Override
//        public T set(int index, T element) {
//            return o.set(index, element);
//        }
//
//        @Override
//        public void add(int index, T element) {
//            o.add(index, element);
//        }
//
//        @Override
//        public T remove(int index) {
//            return o.remove(index);
//        }
//
//        @Override
//        public int indexOf(Object o) {
//            return this.o.indexOf(o);
//        }
//
//        @Override
//        public int lastIndexOf(Object o) {
//            return this.o.lastIndexOf(o);
//        }
//
//        @Override
//        public ListIterator<T> listIterator() {
//            return o.listIterator();
//        }
//
//        @Override
//        public ListIterator<T> listIterator(int index) {
//            return o.listIterator(index);
//        }
//
//        @Override
//        public List<T> subList(int fromIndex, int toIndex) {
//            return o.subList(fromIndex, toIndex);
//        }
//
//        @Override
//        public void addListener(InvalidationListener invalidationListener) {
//            o.addListener(invalidationListener);
//        }
//
//        @Override
//        public void removeListener(InvalidationListener invalidationListener) {
//            o.removeListener(invalidationListener);
//        }
//    }
//
//    public static <T> ObservableList<T> assureJavaFXThreadForListeners(ObservableList<T> list) {
//        return new AssureJavaFXThreadForListenersWrapper(list);
//    }
}
