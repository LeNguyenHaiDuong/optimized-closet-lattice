package utils.utility_transaction_database;
/* This file is copyright (c) 2008-2013 Philippe Fournier-Viger
* 
* This file is part of the SPMF DATA MINING SOFTWARE
* (http://www.philippe-fournier-viger.com/spmf).
* 
* SPMF is free software: you can redistribute it and/or modify it under the
* terms of the GNU General Public License as published by the Free Software
* Foundation, either version 3 of the License, or (at your option) any later
* version.
* 
* SPMF is distributed in the hope that it will be useful, but WITHOUT ANY
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
* A PARTICULAR PURPOSE. See the GNU General Public License for more details.
* You should have received a copy of the GNU General Public License along with
* SPMF. If not, see <http://www.gnu.org/licenses/>.
*/

import java.util.List;


/**
 * This class represents a transaction (a set of items) from a transaction
 * database with utility values, as used by the Two-Phase algorithm for high 
 * utility itemset mining.
 *
 * @see AlgoTwoPhase
 * @see UtilityTransactionDatabaseTP
 * @see ItemUtility
 * 
 * @author Philippe Fournier-Viger
 */
public class TransactionTP{
	// a transaction is an ordered list of items
	protected final List<ItemUtility> itemsUtilities; 
	// the total transaction utility (TU)
	protected final int transactionUtility;
	
	/**
	 * Constructor
	 * @param itemsUtilities list of items
	 * @param itemsUtilities list of corresponding utility values
	 * @param transactionUtility  the transaction utility
	 */
	public TransactionTP(List<ItemUtility> itemsUtilities, int transactionUtility){
		this.itemsUtilities =  itemsUtilities;
		this.transactionUtility = transactionUtility;
	}
	
	/**
	 * Get the list of items.
	 * @return a list of items (Integer)
	 */
	public List<ItemUtility> getItems(){
		return itemsUtilities;
	}
	
	/**
	 * Get the item at a given position.
	 * @param index  the position 
	 * @return the item
	 */
	public ItemUtility get(int index){
		return itemsUtilities.get(index);
	}
	
	/**
	 * Print the transaction to System.out
	 */
	public void print(){
		System.out.print(toString());
	}
	
	/**
	 * Return a string representation of this transaction.
	 */
	public String toString(){
		// create a string buffer
		StringBuilder r = new StringBuilder ();
		// append all items
		for(int i=0; i< itemsUtilities.size(); i++){
			r.append(itemsUtilities.get(i) + " ");
			if(i == itemsUtilities.size() -1){
				r.append(":");
			}
		}
		// append the transaction utility
		r.append(transactionUtility + ": ");
		// append the item utility values
		for(int i=0; i< itemsUtilities.size(); i++){
			r.append(itemsUtilities.get(i) + " ");
		}
		// return the buffer as a string
		return r.toString();
	}

	/**
	 * Check if this transaction contains an item.
	 * @param item the given item
	 * @return true if yes, otherwise false.
	 */
	public boolean contains(Integer item) {
		// for each item in the transaction
		for(ItemUtility itemI : itemsUtilities){
			// if found, return true
			if(itemI.item == item){
				return true;
			}else if(itemI.item > item){
				// if the current item is larger, then the item will not be found
				// because of lexical order so return false
				return false;
			}
		}
		// if not found, return false
		return false;
	}
	
	/**
	 * Check if this transaction contains an item.
	 * @param item the given item
	 * @return true if yes, otherwise false.
	 */
	public boolean contains(int item) {
		// for each item in the transaction
		for(int i=0; i<itemsUtilities.size(); i++){
			// if found, return true
			if(itemsUtilities.get(i).item == item){
				return true;
			}else if(itemsUtilities.get(i).item > item){
				// if the current item is larger, then the item will not be found
				// because of lexical order so return false
				return false;
			}
		}
		// if not found, return false
		return false;
	}


	/**
	 * Get the number of items in this transaction.
	 * @return the item count
	 */
	public int size(){
		return itemsUtilities.size();
	}

	/**
	 * Get the item utilities for this transaction.
	 * @return a list containing the item utilities
	 */
	public List<ItemUtility> getItemsUtilities() {
		return itemsUtilities;
	}

	/**
	 * Get the transaction utility of this transaction.
	 * @return  an integer
	 */
	public int getTransactionUtility() {
		return transactionUtility;
	}

}
