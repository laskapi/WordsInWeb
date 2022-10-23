package com.gmail.in2horizon.wordsinweb;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runner.manipulation.Ordering;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.os.Handler;

import com.gmail.in2horizon.wordsinweb.dictionarymanager.Dictionary;
import com.gmail.in2horizon.wordsinweb.dictionarymanager.DictionaryManager;

import java.util.ArrayList;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */



@RunWith(MockitoJUnitRunner.class)
public class ExampleUnitTest {
    @Mock
    Context context;
@Before
public void init(){
    MockitoAnnotations.openMocks(this);

}
    @Test
    public void addition_isCorrect() {
        when(context.getString(1)).thenReturn("dd");

      /*  try {
            Object o= new Dictionary("s"){
                @Override
                public String getFileName() {
                    return super.getFileName();
                }
            };
        } catch (InstantiationException e) {
            e.printStackTrace();
        }*/

     /*   Object d= new DictionaryManager(context){

            @Override
            public ArrayList<String> getAvailableDictionarySourceNames() {

                return super.getAvailableDictionarySourceNames();
            }
        };*/


                assertEquals(context.getString(1),"cd");
               assertEquals(4, 2 + 2);
    }
    }