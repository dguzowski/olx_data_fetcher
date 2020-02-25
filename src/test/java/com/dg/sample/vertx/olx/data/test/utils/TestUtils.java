package com.dg.sample.vertx.olx.data.test.utils;

import com.dg.sample.vertx.olx.data.webclient.OlxWebClientVerticle;
import com.dg.sample.vertx.olx.data.webclient.helpers.HtmlAsyncDocumentDataFetcher;
import com.dg.sample.vertx.olx.data.webclient.models.DocumentData;
import org.mockito.Mockito;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.Optional;

public final class TestUtils {
  private TestUtils(){}

  public static String getOlxHtmlStubbedData(){
    return getOxlStubbedOffersHtmlPage()
      .orElse("<html></html>");
  }

  public static <T, F> F setUpMockFieldOnObject(T target, Class<F> classToMock, String fieldName ) throws IllegalAccessException, NoSuchFieldException {
    Field field = getDeclaredFieldOnClassHierarchy(target, fieldName)
      .orElseThrow(NoSuchFieldException::new);
    field.setAccessible(true);
    F mock = Mockito.mock(classToMock);
    field.set(target, mock);
    return mock;
  }

  private static <T> Optional<Field> getDeclaredFieldOnClassHierarchy(T target, String fieldName) {
    Field f = null;
    for (Class<? super T> clazz = (Class<? super T>) target.getClass(); f==null && !clazz.getSuperclass().getClass().equals(Object.class); clazz = clazz.getSuperclass()){
      f = tryToGetFIeld(clazz, fieldName);
    }
    return Optional.ofNullable(f);
  }

  private static <T> Field tryToGetFIeld(Class<T> clazz, String fieldName){
    Field field = null;
    try{
      field = clazz.getDeclaredField(fieldName);
    } catch (NoSuchFieldException e) {
      //ignore
    }
    return field;
  }

  private static Optional<String> getOxlStubbedOffersHtmlPage() {
    String result = null;
    try(InputStream searchResultStream = TestUtils.class.getResourceAsStream("/hyundai_search_result.html");
        BufferedReader reader = new BufferedReader(new InputStreamReader(searchResultStream))) {

      result = reader.lines()
        .collect(StringBuilder::new, (b,s) -> { b.append(s); b.append("\n");}, (b1, b2)-> b1.append(b2))
        .toString();

    } catch (IOException e) {
      e.printStackTrace();
    }
    finally {
      return Optional.ofNullable(result);
    }
  }

  public static <T, F> F getFieldValue(T target, String fieldName) throws NoSuchFieldException, IllegalAccessException {
    Field f = getDeclaredFieldOnClassHierarchy(target, fieldName)
      .orElseThrow(NoSuchFieldException::new);
    f.setAccessible(true);
    return (F) f.get(target);
  }
}
