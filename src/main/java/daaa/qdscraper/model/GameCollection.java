package daaa.qdscraper.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.lang3.StringUtils;

public class GameCollection implements List<Game> {

	// our delegate
	private List<Game> games; 

	/** Basic constructor with an empty list of games */
	public GameCollection() {
		games = new ArrayList<Game>();
	}
	
	/** Constructor with some games already */
	public GameCollection(List<Game> games) {
		this.games = new ArrayList<>();
		this.games.addAll(games);
	}
	
	/**
	 * Finds the best perfect match in the list of games, ie with a score of 1 and an image
	 * @param games
	 * @return
	 */
	public Game getBestPerfectMatch()
	{
		GameCollection perfectMatches = getPerfectMatches();
		
		if(perfectMatches.size() > 0)
		{
			Collections.sort(perfectMatches);
			return perfectMatches.get(0);
		}
		
		return null;
	}
	
	/**
	 * Finds all the perfect matches from the list
	 * @return
	 */
	public GameCollection getPerfectMatches()
	{
		GameCollection perfectMatches = new GameCollection();
		for(Game game: games)
		{
			if(game.getScore() == 1.0 && !StringUtils.isEmpty(game.getImage())) {
				perfectMatches.add(game);
			}
		}
		return perfectMatches;
	}
	
	/**
	 * Counts the perfect matches it contains
	 * @return
	 */
	public int countPerfectMatches()
	{
		int nb = 0;
		for(Game game: games)
		{
			if(game.getScore() == 1.0 && !StringUtils.isEmpty(game.getImage())) {
				nb ++;
			}
		}
		return nb;
	}
	
	/**
	 * Sort by best match, first item is then the best
	 */
	public void sortByBestMatch()
	{
		// the game comparator is by best match
		Collections.sort(games);
	}
	
	
	// delegate methods
	
	
	/**
	 * @return
	 * @see java.util.List#size()
	 */
	public int size() {
		return games.size();
	}

	/**
	 * @return
	 * @see java.util.List#isEmpty()
	 */
	public boolean isEmpty() {
		return games.isEmpty();
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.List#contains(java.lang.Object)
	 */
	public boolean contains(Object o) {
		return games.contains(o);
	}

	/**
	 * @return
	 * @see java.util.List#iterator()
	 */
	public Iterator<Game> iterator() {
		return games.iterator();
	}

	/**
	 * @return
	 * @see java.util.List#toArray()
	 */
	public Object[] toArray() {
		return games.toArray();
	}

	/**
	 * @param a
	 * @return
	 * @see java.util.List#toArray(T[])
	 */
	public <T> T[] toArray(T[] a) {
		return games.toArray(a);
	}

	/**
	 * @param e
	 * @return
	 * @see java.util.List#add(java.lang.Object)
	 */
	public boolean add(Game e) {
		return games.add(e);
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.List#remove(java.lang.Object)
	 */
	public boolean remove(Object o) {
		return games.remove(o);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.List#containsAll(java.util.Collection)
	 */
	public boolean containsAll(Collection<?> c) {
		return games.containsAll(c);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.List#addAll(java.util.Collection)
	 */
	public boolean addAll(Collection<? extends Game> c) {
		return games.addAll(c);
	}

	/**
	 * @param index
	 * @param c
	 * @return
	 * @see java.util.List#addAll(int, java.util.Collection)
	 */
	public boolean addAll(int index, Collection<? extends Game> c) {
		return games.addAll(index, c);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.List#removeAll(java.util.Collection)
	 */
	public boolean removeAll(Collection<?> c) {
		return games.removeAll(c);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.List#retainAll(java.util.Collection)
	 */
	public boolean retainAll(Collection<?> c) {
		return games.retainAll(c);
	}

	/**
	 * 
	 * @see java.util.List#clear()
	 */
	public void clear() {
		games.clear();
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.List#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		return games.equals(o);
	}

	/**
	 * @return
	 * @see java.util.List#hashCode()
	 */
	public int hashCode() {
		return games.hashCode();
	}

	/**
	 * @param index
	 * @return
	 * @see java.util.List#get(int)
	 */
	public Game get(int index) {
		return games.get(index);
	}

	/**
	 * @param index
	 * @param element
	 * @return
	 * @see java.util.List#set(int, java.lang.Object)
	 */
	public Game set(int index, Game element) {
		return games.set(index, element);
	}

	/**
	 * @param index
	 * @param element
	 * @see java.util.List#add(int, java.lang.Object)
	 */
	public void add(int index, Game element) {
		games.add(index, element);
	}

	/**
	 * @param index
	 * @return
	 * @see java.util.List#remove(int)
	 */
	public Game remove(int index) {
		return games.remove(index);
	}


	/**
	 * @return
	 * @see java.util.List#listIterator()
	 */
	public ListIterator<Game> listIterator() {
		return games.listIterator();
	}

	/**
	 * @param index
	 * @return
	 * @see java.util.List#listIterator(int)
	 */
	public ListIterator<Game> listIterator(int index) {
		return games.listIterator(index);
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.List#indexOf(java.lang.Object)
	 */
	public int indexOf(Object o) {
		return games.indexOf(o);
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.List#lastIndexOf(java.lang.Object)
	 */
	public int lastIndexOf(Object o) {
		return games.lastIndexOf(o);
	}

	/**
	 * @param fromIndex
	 * @param toIndex
	 * @return
	 * @see java.util.List#subList(int, int)
	 */
	public List<Game> subList(int fromIndex, int toIndex) {
		return games.subList(fromIndex, toIndex);
	}

	
	

}
