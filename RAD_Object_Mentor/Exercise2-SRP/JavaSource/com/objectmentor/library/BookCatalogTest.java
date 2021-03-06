package com.objectmentor.library;

import java.util.List;

import junit.framework.TestCase;

import com.objectmentor.library.data.IsbnDoesNotExistException;
import com.objectmentor.library.mocks.MockDataServices;
import com.objectmentor.library.models.BookCopy;
import com.objectmentor.library.models.BookTitle;
import com.objectmentor.library.models.Patron;
import com.objectmentor.library.models.Receipt;

public class BookCatalogTest extends TestCase {

	private MockDataServices dataServices;
	private BookCatalog catalog;

	protected void setUp() throws Exception {
		dataServices = new MockDataServices();
		catalog = new BookCatalog(dataServices);
	}

	public void testAddBookShouldLookUpISBN() {
		addCopy("ISBN");
		assertEquals("ISBN", dataServices.wasLastCalledWithThisIsbn);
	}

	public void testAddBookShouldAddBookReturnedByIsbnService() {
		BookCopy bookCopy = addCopy("ISBN");
		assertEquals(dataServices.addedBookCopy.getBookTitle(), bookCopy
				.getBookTitle());
	}

	public void testShouldReturnCorrectBookAmongMany() {
		BookCopy copy1 = addCopy("ISBN 1");
		BookCopy copy2 = addCopy("ISBN 2");
		assertSame(copy1, catalog.findCopy("ISBN 1"));
		assertSame(copy2, catalog.findCopy("ISBN 2"));
	}

	public void testUnfoundISBN() {
		try {
			catalog.addCopy("NON-EXISTENT ISBN");
			fail();
		} catch (IsbnDoesNotExistException e) {
		}
	}

	public void testCanFindMoreThanOneBook() throws Exception {
		BookCopy copy1 = addCopy("ISBN");
		BookCopy copy2 = addCopy("ISBN");
		List<BookCopy> copies = catalog.findAllCopies("ISBN");
		assertEquals(2, copies.size());
		assertTrue(copies.contains(copy1));
		assertTrue(copies.contains(copy2));
	}

	public void testFindAvailableCopyReturnsCopyWhenOneCopyOfManyIsBorrowed()
			throws Exception {
		addCopy("ISBN");
		addCopy("ISBN");
		List<BookCopy> copies = catalog.findAllCopies("ISBN");
		BookCopy copy = (BookCopy) copies.get(0);
		copy.setBorrowed(new Receipt(new Patron("borrower"),
				Receipt.BORROW_RECEIPT));
		assertNotNull(catalog.findAvailableCopy("ISBN"));
	}

	private BookCopy addCopy(String isbn) {
		dataServices.setBookToReturn(new BookTitle(isbn));
		return catalog.addCopy(isbn);
	}

}
