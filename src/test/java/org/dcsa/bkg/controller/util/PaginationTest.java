package org.dcsa.bkg.controller.util;

import org.dcsa.core.exception.CreateException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.util.MultiValueMap;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;


public class PaginationTest {

  @Nested
  class ParseCursorTest {
    private final Pagination pagination = new Pagination(Sort.by(Sort.Direction.DESC, "foo"));

    @Test
    void parseCursorTest() {
      String cursor =
          Base64.getUrlEncoder()
              .encodeToString("page=2&size=10&sort=foo: DESC".getBytes(StandardCharsets.UTF_8));
      PageRequest pageRequest = pagination.parseCursor(cursor);
      Assertions.assertEquals(2, pageRequest.getPageNumber());
      Assertions.assertEquals(10, pageRequest.getPageSize());
      Assertions.assertEquals("foo", pageRequest.getSort().get().findFirst().get().getProperty());
    }

    @Test
    void parseCursorTestMultipleSort() {
      String cursor =
          Base64.getUrlEncoder()
              .encodeToString(
                  "page=2&size=10&sort=foo: DESC,bar: ASC".getBytes(StandardCharsets.UTF_8));
      PageRequest pageRequest = pagination.parseCursor(cursor);
      Assertions.assertEquals(2, pageRequest.getPageNumber());
      Assertions.assertEquals(10, pageRequest.getPageSize());

      Assertions.assertEquals(
          "foo",
          pageRequest
              .getSort()
              .get()
              .filter(order -> order.getProperty().equals("foo"))
              .findFirst()
              .get()
              .getProperty());
      Assertions.assertEquals(
          "bar",
          pageRequest
              .getSort()
              .get()
              .filter(order -> order.getProperty().equals("bar"))
              .findFirst()
              .get()
              .getProperty());
      Assertions.assertEquals(
          Sort.Direction.DESC, pageRequest.getSort().getOrderFor("foo").getDirection());
      Assertions.assertEquals(
          Sort.Direction.ASC, pageRequest.getSort().getOrderFor("bar").getDirection());
    }

    @Test
    void parseMalformedCursorTest() {
      String cursor =
          Base64.getUrlEncoder()
              .encodeToString("page=2&sort=foo:DESC,bar:ASC".getBytes(StandardCharsets.UTF_8));
      CreateException createException =
          Assertions.assertThrows(
              CreateException.class,
              () -> {
                pagination.parseCursor(cursor);
              });
      Assertions.assertEquals("Malformed cursor", createException.getMessage());
    }
	}

	@Nested
	class ParseSortTest {

		private final Pagination pagination = new Pagination(Sort.by(Sort.Direction.DESC, "bookingRequestDateTime"));

		@Test
		void parseSingleSortTest() {
			String[] sortParam = {"foo: ASC"};
			Sort sort = pagination.parseSort(sortParam);

			Assertions.assertEquals("foo", sort.get().findFirst().get().getProperty());
			Assertions.assertEquals(Sort.Direction.ASC, sort.getOrderFor("foo").getDirection());
		}

		@Test
		void ParseSingleSortDefaultDirection() {
			String[] sortParam = {"foo"};
			Sort sort = pagination.parseSort(sortParam);

			Assertions.assertEquals("foo", sort.get().findFirst().get().getProperty());
			Assertions.assertEquals(Sort.Direction.DESC, sort.getOrderFor("foo").getDirection());
		}

		@Test
		void parseMultipleSortTest() {
			String[] sortParam = {"foo: ASC", "bar: DESC"};
			Sort sort = pagination.parseSort(sortParam);

			Assertions.assertEquals("foo", sort.get().filter(order -> order.getProperty().equals("foo")).findFirst().get().getProperty());
			Assertions.assertEquals("bar", sort.get().filter(order -> order.getProperty().equals("bar")).findFirst().get().getProperty());
			Assertions.assertEquals(Sort.Direction.ASC, sort.getOrderFor("foo").getDirection());
			Assertions.assertEquals(Sort.Direction.DESC, sort.getOrderFor("bar").getDirection());
		}

		@Test
		void parseMultipleSortDefaultDirectionTest() {
			String[] sortParam = {"foo", "bar"};
			Sort sort = pagination.parseSort(sortParam);

			Assertions.assertEquals("foo", sort.get().filter(order -> order.getProperty().equals("foo")).findFirst().get().getProperty());
			Assertions.assertEquals("bar", sort.get().filter(order -> order.getProperty().equals("bar")).findFirst().get().getProperty());
			Assertions.assertEquals(Sort.Direction.DESC, sort.getOrderFor("foo").getDirection());
			Assertions.assertEquals(Sort.Direction.DESC, sort.getOrderFor("bar").getDirection());
		}

		@Test
		void parseMultipleSortOneDefaultDirectionTest() {
			String[] sortParam = {"foo", "bar: ASC"};
			Sort sort = pagination.parseSort(sortParam);

			Assertions.assertEquals("foo", sort.get().filter(order -> order.getProperty().equals("foo")).findFirst().get().getProperty());
			Assertions.assertEquals("bar", sort.get().filter(order -> order.getProperty().equals("bar")).findFirst().get().getProperty());
			Assertions.assertEquals(Sort.Direction.DESC, sort.getOrderFor("foo").getDirection());
			Assertions.assertEquals(Sort.Direction.ASC, sort.getOrderFor("bar").getDirection());
		}

		@Test
		void parseDefaultSort() {
			Sort sort = pagination.parseSort(null);
			Assertions.assertEquals("bookingRequestDateTime", sort.get().filter(order -> order.getProperty().equals("bookingRequestDateTime")).findFirst().get().getProperty());
			Assertions.assertEquals(Sort.Direction.DESC, sort.getOrderFor("bookingRequestDateTime").getDirection());
		}

		@Test
		void parseEmptyDefaultSort() {
			String [] sortParam = {};
			Sort sort = pagination.parseSort(sortParam);
			Assertions.assertEquals("bookingRequestDateTime", sort.get().filter(order -> order.getProperty().equals("bookingRequestDateTime")).findFirst().get().getProperty());
			Assertions.assertEquals(Sort.Direction.DESC, sort.getOrderFor("bookingRequestDateTime").getDirection());
		}
	}

	@Nested
	class FormatCursorTest {

		private final Pagination pagination = new Pagination(Sort.by(Sort.Direction.DESC, "bookingRequestDateTime"));

		@Test
		void formatCursorTest() {
			Sort sort = Sort.by(Sort.Direction.DESC, "foo");
			String expectedCursor = Base64.getUrlEncoder().encodeToString(("page=0&size=10&sort="+ sort).getBytes(StandardCharsets.UTF_8));
			String formattedCursor =  pagination.formatCursor(0, 10, sort);
			Assertions.assertEquals(expectedCursor, formattedCursor);
		}
	}

	@Nested
	class SetPaginationHeadersTest {

		private final Pagination pagination = new Pagination(Sort.by(Sort.Direction.DESC, "bookingRequestDateTime"));

		@Test
		void testSetPaginationHeadersWithDefaultSort() {
			Sort sort = pagination.parseSort(null);
			PageRequest pageRequest = PageRequest.of(0,10, sort);
			Page<String> page = new PageImpl<String>(Arrays.asList("test"), pageRequest, 100);

			MultiValueMap<String, String> headers = pagination.setPaginationHeaders(page);
			Assertions.assertTrue(headers.containsKey("Current-Page"));
			Assertions.assertTrue(headers.containsKey("Next-Page"));
			Assertions.assertTrue(headers.containsKey("Last-Page"));

			Assertions.assertEquals(Base64.getUrlEncoder().encodeToString(("page=0&size=10&sort="+sort).getBytes(StandardCharsets.UTF_8)), headers.get("Current-Page").get(0));
			Assertions.assertEquals(Base64.getUrlEncoder().encodeToString(("page=1&size=10&sort="+sort).getBytes(StandardCharsets.UTF_8)), headers.get("Next-Page").get(0));
			Assertions.assertEquals(Base64.getUrlEncoder().encodeToString(("page=9&size=10&sort="+sort).getBytes(StandardCharsets.UTF_8)), headers.get("Last-Page").get(0));
		}


		@Test
		void testSetPaginationHeadersWithCustomSort() {
			Sort sort = pagination.parseSort(new String[]{"Foo:DESC"});
			PageRequest pageRequest = PageRequest.of(0,5, sort);
			Page<String> page = new PageImpl<String>(Arrays.asList("test"), pageRequest, 100);

			MultiValueMap<String, String> headers = pagination.setPaginationHeaders(page);
			Assertions.assertTrue(headers.containsKey("Current-Page"));
			Assertions.assertTrue(headers.containsKey("Next-Page"));
			Assertions.assertTrue(headers.containsKey("Last-Page"));

			Assertions.assertEquals(Base64.getUrlEncoder().encodeToString(("page=0&size=5&sort="+sort).getBytes(StandardCharsets.UTF_8)), headers.get("Current-Page").get(0));
			Assertions.assertEquals(Base64.getUrlEncoder().encodeToString(("page=1&size=5&sort="+sort).getBytes(StandardCharsets.UTF_8)), headers.get("Next-Page").get(0));
			Assertions.assertEquals(Base64.getUrlEncoder().encodeToString(("page=19&size=5&sort="+sort).getBytes(StandardCharsets.UTF_8)), headers.get("Last-Page").get(0));
		}

		@Test
		void testPaginationMiddleRequest() {
			Sort sort = pagination.parseSort(new String[]{"Foo:DESC"});
			PageRequest pageRequest = PageRequest.of(3,5, sort);
			Page<String> page = new PageImpl<String>(Arrays.asList("test"), pageRequest, 100);

			MultiValueMap<String, String> headers = pagination.setPaginationHeaders(page);
			Assertions.assertTrue(headers.containsKey("Current-Page"));
			Assertions.assertTrue(headers.containsKey("Next-Page"));
			Assertions.assertTrue(headers.containsKey("Last-Page"));

			Assertions.assertEquals(Base64.getUrlEncoder().encodeToString(("page=3&size=5&sort="+sort).getBytes(StandardCharsets.UTF_8)), headers.get("Current-Page").get(0));
			Assertions.assertEquals(Base64.getUrlEncoder().encodeToString(("page=4&size=5&sort="+sort).getBytes(StandardCharsets.UTF_8)), headers.get("Next-Page").get(0));
			Assertions.assertEquals(Base64.getUrlEncoder().encodeToString(("page=19&size=5&sort="+sort).getBytes(StandardCharsets.UTF_8)), headers.get("Last-Page").get(0));
		}

		@Test
		void testCurrentPageIsLastPage() {
			Sort sort = pagination.parseSort(new String[]{"Foo:DESC"});
			PageRequest pageRequest = PageRequest.of(19,5, sort);
			Page<String> page = new PageImpl<String>(Arrays.asList("test"), pageRequest, 100);

			MultiValueMap<String, String> headers = pagination.setPaginationHeaders(page);
			Assertions.assertTrue(headers.containsKey("Current-Page"));
			Assertions.assertFalse(headers.containsKey("Next-Page"));
			Assertions.assertFalse(headers.containsKey("Last-Page"));

			Assertions.assertEquals(Base64.getUrlEncoder().encodeToString(("page=19&size=5&sort="+sort).getBytes(StandardCharsets.UTF_8)), headers.get("Current-Page").get(0));
		}

		@Test
		void testNoExtraPage() {
			Sort sort = pagination.parseSort(new String[]{"Foo:DESC"});
			PageRequest pageRequest = PageRequest.of(0,5, sort);
			Page<String> page = new PageImpl<String>(Arrays.asList("test"), pageRequest, 5);

			MultiValueMap<String, String> headers = pagination.setPaginationHeaders(page);
			Assertions.assertTrue(headers.containsKey("Current-Page"));
			Assertions.assertFalse(headers.containsKey("Next-Page"));
			Assertions.assertFalse(headers.containsKey("Last-Page"));

			Assertions.assertEquals(Base64.getUrlEncoder().encodeToString(("page=0&size=5&sort="+sort).getBytes(StandardCharsets.UTF_8)), headers.get("Current-Page").get(0));
		}
	}
}
