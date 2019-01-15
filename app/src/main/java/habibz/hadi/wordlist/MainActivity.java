package habibz.hadi.wordlist;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.content.Intent;

public class MainActivity extends AppCompatActivity
{
    private WordListDatabase wordList;
    private static final String logTag = "MainActivityLogTag";

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );
        setTitle( "Welcome" );
        this.wordList = new WordListDatabase( this );
        this.wordList.buildLocalJsonLibrary();
    } // end method onCreate

    public void quickStartButton( View view )
    {
        Intent intent = new Intent( this, ShowListActivity.class );
        startActivity( intent );
    } // end method quickStartButton

} // end class MainActivity
