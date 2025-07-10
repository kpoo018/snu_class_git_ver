import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Genre, Title 을 관리하는 영화 데이터베이스.
 * 
 * MyLinkedList 를 사용해 각각 Genre와 Title에 따라 내부적으로 정렬된 상태를  
 * 유지하는 데이터베이스이다. 
 */
public class MovieDB {
	private MyLinkedList<MovieDBItem> MyMovieDB;
    public MovieDB() {
        // FIXME implement this
		MyMovieDB=new MyLinkedList<>();

    	// HINT: MovieDBGenre 클래스를 정렬된 상태로 유지하기 위한 
    	// MyLinkedList 타입의 멤버 변수를 초기화 한다.
    }

    public void insert(MovieDBItem item) {

		if(MyMovieDB.isEmpty()){
			MyMovieDB.add(item);
			return;
		}

		MovieDBItem movieDBItem = null;
		MyLinkedListIterator<MovieDBItem> movieDBItemIterator = (MyLinkedListIterator<MovieDBItem>)MyMovieDB.iterator();

		while (movieDBItemIterator.hasNext()){
			movieDBItem=movieDBItemIterator.next();
			if(movieDBItem.compareTo(item)<0) continue;
			if(movieDBItem.compareTo(item)==0) return;
			if(movieDBItem.compareTo(item)>0){
				movieDBItemIterator.insert(item);
				return;
			}
		}

		MyMovieDB.add(item);



        //System.err.printf("[trace] MovieDB: INSERT [%s] [%s]\n", item.getGenre(), item.getTitle());
    }

    public void delete(MovieDBItem item) {

		MovieDBItem movieDBItem = null;
		MyLinkedListIterator<MovieDBItem> movieDBItemIterator = (MyLinkedListIterator<MovieDBItem>)MyMovieDB.iterator();

		while (movieDBItemIterator.hasNext()){
			movieDBItem=movieDBItemIterator.next();
			if(movieDBItem.compareTo(item)==0){
				movieDBItemIterator.remove();
				break;
			}
		}



        //System.err.printf("[trace] MovieDB: DELETE [%s] [%s]\n", item.getGenre(), item.getTitle());
    }

    public MyLinkedList<MovieDBItem> search(String term) {

    	//System.err.printf("[trace] MovieDB: SEARCH [%s]\n", term);


        MyLinkedList<MovieDBItem> results = new MyLinkedList<>();


		MyLinkedListIterator<MovieDBItem> movieDBItemIterator = (MyLinkedListIterator<MovieDBItem>)MyMovieDB.iterator();
		MovieDBItem movieDBItem = null;

		while (movieDBItemIterator.hasNext()){
			movieDBItem=movieDBItemIterator.next();
			if (movieDBItem.getTitle().contains(term)){
				results.add(movieDBItem);
			}
		}

		return results;
    }
    
    public MyLinkedList<MovieDBItem> items() {

        //System.err.printf("[trace] MovieDB: ITEMS\n");

        MyLinkedList<MovieDBItem> results = MyMovieDB;
        
    	return results;
    }
}

//class Genre implements Comparable<Genre> {
//	private String item;
//	private MyMovieList list;
//	public Genre(String name) {
//		this.item=name;
//		//throw new UnsupportedOperationException("not implemented yet");
//	}
//
//	public String getItem() {
//		return item;
//	}
//	public MyMovieList getList() {
//		return list;
//	}
//	public void setList(MyMovieList input){
//		this.list=input;
//	}
//
//	public void addInList(String title){
//		this.list.add(title);
//	}
//
//	@Override
//	public int compareTo(Genre o) {
//		if (this.getItem().compareTo(o.getItem()) > 0) return 1;
//		else if (this.getItem().compareTo(o.getItem()) < 0) return -1;
//		else return 0;
//		//throw new UnsupportedOperationException("not implemented yet");
//	}
//
//	@Override
//	public int hashCode() {
//		final int prime = 31;
//		int result = 1;
//		result = prime * result + (this.getItem().hashCode());
//		return result;
//		//throw new UnsupportedOperationException("not implemented yet");
//	}
//
//	@Override
//	public boolean equals(Object obj) {
//		if (this == obj)
//			return true;
//		if (obj == null)
//			return false;
//		if (getClass() != obj.getClass())
//			return false;
//		Genre other = (Genre) obj;
//		if (this.getItem() != other.getItem()){
//			return false;
//		}
//		return true;
//		//throw new UnsupportedOperationException("not implemented yet");
//	}
//}




