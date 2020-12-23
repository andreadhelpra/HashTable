package ci583.htable.impl;

/**
 * A HashTable with no deletions allowed. Duplicates overwrite the existing value. Values are of
 * type V and keys are strings -- one extension is to adapt this class to use other types as keys.
 * 
 * The underlying data is stored in the array `arr', and the actual values stored are pairs of 
 * (key, value). This is so that we can detect collisions in the hash function and look for the next 
 * location when necessary.
 */

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;


public class Hashtable<V> {

	private Object[] arr; //an array of Pair objects, where each pair contains the key and value stored in the hashtable
	private int max; //the size of arr. This should be a prime number
	private int itemCount; //the number of items stored in arr
	private final double maxLoad = 0.6; //the maximum load factor

	public static enum PROBE_TYPE {
		LINEAR_PROBE, QUADRATIC_PROBE, DOUBLE_HASH;
	}

	PROBE_TYPE probeType; //the type of probe to use when dealing with collisions
	private final BigInteger DBL_HASH_K = BigInteger.valueOf(8);

	/**
	 * Create a new Hashtable with a given initial capacity and using a given probe type
	 * @param initialCapacity
	 * @param pt
	 */
	public Hashtable(int initialCapacity, PROBE_TYPE pt) {
		/* If initialCapacity is already a prime number, nextPrime will return it
         * If not it will change it to the smallest larger prime */
        this.max = nextPrime(initialCapacity);
		this.probeType = pt;
		this.arr = new Object[max];
	}
	
	/**
	 * Create a new Hashtable with a given initial capacity and using the default probe type
	 * @param initialCapacity
	 */
	public Hashtable(int initialCapacity) {
		// Call the other constructor and set LINEAR_PROBE to the probe type
		this(initialCapacity, PROBE_TYPE.LINEAR_PROBE);
	}

	/**
	 * Store the value against the given key. If the loadFactor exceeds maxLoad, call the resize 
	 * method to resize the array. the If key already exists then its value should be overwritten.
	 * Create a new Pair item containing the key and value, then use the findEmpty method to find an unoccupied 
	 * position in the array to store the pair. Call findEmpty with the hashed value of the key as the starting
	 * position for the search, stepNum of zero and the original key.
	 * containing   
	 * @param key
	 * @param value
	 */
	public void put(String key, V value) {
	    // If key is null an exception will be thrown
	    try{
            if (getLoadFactor()>maxLoad) {
                resize();
            }
            int position = findEmpty(hash(key), 0, key); // Unoccupied or need-to-overwrite position
            if (arr[position]==null) {
                itemCount++; // Add 1 to item count only when the position is empty
            }
            arr[position] = new Pair(key, value);// Put the new object at the right position
	    }catch (Exception e){
            throw new IllegalArgumentException("key is null" + e);
        }
	}

	/**
	 * Get the value associated with key, or return null if key does not exists. Use the find method to search the
	 * array, starting at the hashed value of the key, stepNum of zero and the original key.
	 * @param key
	 * @return
	 */
    // The find method will return null when key is null, otherwise it will return the value associated with key
	public V get(String key) {return find(hash(key), key, 0);}

	/**
	 * Return true if the Hashtable contains this key, false otherwise 
	 * @param key
	 * @return
	 */
	// When the value returned by the get method is not null return true, false otherwise
	public boolean hasKey(String key) {return get(key) != null;}

	/**
	 * Return all the keys in this Hashtable as a collection
	 * @return
	 */
	public Collection<String> getKeys() {
		Collection<String> keys = new ArrayList<>(); // Create a new ArrayList
		for (int i=0; i<max; i++){
		    if (arr[i]!=null){
                Pair obj = (Pair)arr[i];
                keys.add(obj.key); // If the position contains a key add this to the new ArrayList
		    }
		}
		return keys;
	}

	/**
	 * Return the load factor, which is the ratio of itemCount to max
	 * @return
	 */
	public double getLoadFactor() {return (double) itemCount/max;} // Convert int into double

	/**
	 * return the maximum capacity of the Hashtable
	 * @return
	 */
	public int getCapacity() {return max;}
	
	/**
	 * Find the value stored for this key, starting the search at position startPos in the array. If
	 * the item at position startPos is null, the Hashtable does not contain the value, so return null. 
	 * If the key stored in the pair at position startPos matches the key we're looking for, return the associated 
	 * value. If the key stored in the pair at position startPos does not match the key we're looking for, this
	 * is a hash collision so use the getNextLocation method with an incremented value of stepNum to find 
	 * the next location to search (the way that this is calculated will differ depending on the probe type 
	 * being used). Then use the value of the next location in a recursive call to find.
	 * @param startPos
	 * @param key
	 * @param stepNum
	 * @return
	 */
	private V find(int startPos, String key, int stepNum) {
		Pair obj = (Pair)arr[startPos];
		if (arr[startPos] == null){
			return null; // Return null when the item at this position is null
		}else if (obj.key.equals(key)){
			return obj.value; // Return the value when key is found
		}else { // If key was not found use a recursion starting at the next location
			int nextPos = getNextLocation(startPos,stepNum +1,key);
			return find(nextPos, key, stepNum+1);
		}
	}

	/**
	 * Find the first unoccupied location where a value associated with key can be stored, starting the
	 * search at position startPos. If startPos is unoccupied, return startPos. Otherwise use the getNextLocation
	 * method with an incremented value of stepNum to find the appropriate next position to check 
	 * (which will differ depending on the probe type being used) and use this in a recursive call to findEmpty.
	 * @param startPos
	 * @param stepNum
	 * @param key
	 * @return
	 */
	private int findEmpty(int startPos, int stepNum, String key) {
        Pair obj = (Pair)arr[startPos];
	    if (arr[startPos]==null || obj.key.equals(key)){
	        return startPos; // When the position is unoccupied or must be overwritten return this
        }
	    else { // Otherwise call findEmpty recursively starting at the next location
            return findEmpty(getNextLocation(startPos, stepNum + 1, key), stepNum + 1, key);
        }
	}

	/**
	 * Finds the next position in the Hashtable array starting at position startPos. If the linear
	 * probe is being used, we just increment startPos. If the double hash probe type is being used, 
	 * add the double hashed value of the key to startPos. If the quadratic probe is being used, add
	 * the square of the step number to startPos.
	 * @param startPos
	 * @param stepNum
	 * @param key
	 * @return
	 */
	private int getNextLocation(int startPos, int stepNum, String key) {
		int step = startPos;
		switch (probeType) {
		case LINEAR_PROBE:
			step++;
			break;
		case DOUBLE_HASH:
			step += doubleHash(key);
			break;
		case QUADRATIC_PROBE:
			step += stepNum * stepNum;
			break;
		default:
			break;
		}
		return step % max;
	}

	/**
	 * A secondary hash function which returns a small value (less than or equal to DBL_HASH_K)
	 * to probe the next location if the double hash probe type is being used
	 * @param key
	 * @return
	 */
	private int doubleHash(String key) {
		BigInteger hashVal = BigInteger.valueOf(key.charAt(0) - 96);
		for (int i = 0; i < key.length(); i++) {
			BigInteger c = BigInteger.valueOf(key.charAt(i) - 96);
			hashVal = hashVal.multiply(BigInteger.valueOf(27)).add(c);
		}
		return DBL_HASH_K.subtract(hashVal.mod(DBL_HASH_K)).intValue();
	}

	/**
	 * Return an int value calculated by hashing the key. See the lecture slides for information
	 * on creating hash functions. The return value should be less than max, the maximum capacity 
	 * of the array
	 * @param key
	 * @return
	 */
	private int hash(String key) {
	   int hash = 0; // Hash initialised to 0
       for(int i=0; i<key.length();i++){
           hash = (hash * 31 + key.charAt(i))%max; // Generate a unique hash value
       }
       return hash % max; // Return the modulus of the hash value and max to make the generated value smaller
	}

	/**
	 * Return true if n is prime
	 * @param n
	 * @return
	 */
	private boolean isPrime(int n) {
        if (n == 2) {return true;} // 2 is a prime number
        if (n<2 || n%2==0) {return false;} // Numbers smaller than 2 and even numbers are not prime
		for (int i=3; i*i<=n; i+=2){ // Divide n by odd numbers
			if (n%i==0) {return false;} // If its modulus is 0 it is not prime
		}
    	return true;
	}

	/**
	 * Get the smallest prime number which is larger than n
	 * @param n
	 * @return
	 */
	private int nextPrime(int n){
	    if (n % 2 == 0) { // If n is even make it odd
            n++;
        }
	    return (isPrime(n)) ? n : nextPrime(n+=2); // Return n if prime or start recursion with the next odd number
	}

	/**
	 * Resize the hashtable, to be used when the load factor exceeds maxLoad. The new size of
	 * the underlying array should be the smallest prime number which is at least twice the size
	 * of the old array.
	 */
	private void resize() {
        max = nextPrime(max * 4); // New capacity is the next prime number at least four times the old capacity
        Object[] oldArr = arr; // Save the old array
        arr = new Object[max]; // Create a new array with the new capacity
        for (Object current : oldArr) { // Pass each item that is not null from the old array to the new one
            if (current != null) {
                Pair obj = (Pair)current;
                put(obj.key, obj.value);
            }
        }
    }

	
	/**
	 * Instances of Pair are stored in the underlying array. We can't just store
	 * the value because we need to check the original key in the case of collisions.
	 * @author jb259
	 */
	private class Pair {
		private String key;
		private V value;

		public Pair(String key, V value) {
			this.key = key;
			this.value = value;
		}
	}

}