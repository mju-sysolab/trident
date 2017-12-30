/*
 *
 LA-CC 05-135 Trident 0.7.1

Copyright Notice
Copyright 2006 (c) the Regents of the University of California.

This Software was produced under a U.S. Government contract
(W-7405-ENG-36) by Los Alamos National Laboratory, which is operated
by the University of California for the U.S. Department of Energy. The
U.S. Government is licensed to use, reproduce, and distribute this
Software. Permission is granted to the public to copy and use this
Software without charge, provided that this Notice and any statement
of authorship are reproduced on all copies. Neither the Government nor
the University makes any warranty, express or implied, or assumes any
liability or responsibility for the user of this Software.


 */


package fp.graph;

import java.util.*;

/**
 * Utility class that obeys the constraints of the Set interface but also
 * preserves the order in which the elements are added
 */
public class OrderedSet extends HashSet {
  List _list;

  public OrderedSet() {
    _list = new ArrayList();
  }

  public boolean add(Object o) {
    boolean success = super.add(o);
    if (success) {
      _list.add(o);
    } // end of if ()
    return success;
  }

  public void clear() {
    super.clear();
    _list.clear();
  }

  public Iterator iterator() {
    return new Iterator() { 
	Object _lastReturned = null;
	Iterator _listIter = _list.iterator();

	public Object next() {
	  _lastReturned = _listIter.next();
	  return _lastReturned;
	}

	public boolean hasNext() {
	  return _listIter.hasNext();
	}

	public void remove() {
	  if (_lastReturned == null) {
	    throw new IllegalStateException();
	  } // end of if ()
	  superRemove(_lastReturned);
	  _listIter.remove();
	}
      };
  }

  public boolean remove(Object o) {
    boolean success = superRemove(o);
    if (success) {
      _list.remove(o);
    } // end of if ()
    return success;
  }

  private boolean superRemove(Object o) {
    return super.remove(o);
  }

  public Object[] toArray() {
    return _list.toArray();
  }
}

