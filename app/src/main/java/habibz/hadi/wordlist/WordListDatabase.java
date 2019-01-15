package habibz.hadi.wordlist;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;

public class WordListDatabase
{
    private Vector< WordEntity > wordList = new Vector<WordEntity>();
    private Context context;
    private static final String logTag = "WordListDatabaseLogTag";
    private static final String databaseFilename = "database.txt";

    public WordListDatabase( final Context context )
    {
        this.context = context;
        this.loadAllWordsFromFile();
    } // end constructor

    // Return a word in the list in the give location
    // Check for boundaries and ranges. If it is out
    // of range, return a fixed word.
    public WordEntity getEntry( int index )
    {

        if( index < 0 || index >= this.wordList.size() )
        {
            WordEntity word = new WordEntity( "Warble (n, v)" );
            Log.d( logTag, "Out of range index. Returned Warble by default" );
            return word;
        }

       return this.wordList.elementAt( index );

    } // end method getEntry

    public int getWordListSize()
    {
        return this.wordList.size();
    } // end method getWordListSize

    public void buildLocalJsonLibrary()
    {
        AsyncDefinitionReader jsonBuilderThread = new AsyncDefinitionReader( context );
        jsonBuilderThread.execute( this );
    } // end method buildLocalJsonLibrary

    // All words must be saved in a txt file saved in the
    // assets directory. Each line of the file must contain
    // only one word and its part of speech. Here is an example
    // line in the file
    // Warble (n, v)
    // This function reads all words in the file and saves
    // them into a vector.
    private void loadAllWordsFromFile()
    {
        AssetManager assetManager = this.context.getAssets();
        InputStreamReader inputStreamReader = null;
        BufferedReader reader = null;

        try
        {
            inputStreamReader = new InputStreamReader( assetManager.open( databaseFilename ) );
            reader = new BufferedReader( inputStreamReader );
            String currentLine = reader.readLine();

            while( currentLine != null )
            {
                WordEntity currentWord = new WordEntity( currentLine );
                this.wordList.add( currentWord );
                currentLine = reader.readLine();
            } // end while

            reader.close();
            inputStreamReader.close();
        }

        catch( IOException e )
        {
            DisplayToast.show( "Cannot read database", this.context, "long" );
            Log.d( logTag, "Failed to read words file." );
            Log.d( logTag, e.getMessage() );
        }

    } // end method loadAllWordsFromFile

    // This class creates a new threat for extracting the definition of word,
    // preferably by reading the local JSON file that contains the definition
    // (if it exists). If need be, it fetches the definition from the Internet.
    // It uses Merriam-Webster dictionary API
    private class AsyncDefinitionReader extends AsyncTask< WordListDatabase, String, String >
    {
        private Context context;

        // A workaround to pass the context to the background thread
        // We need the context for reading and writing files.
        public AsyncDefinitionReader( final Context context )
        {
            this.context = context;
        } // end constructor

        private static final String logTag = "AsyncDefinitionReaderLogTag";

        @Override
        protected String doInBackground( WordListDatabase... database )
        {
            int index;
            int counter = 0;

            for( index = 0; index < database[0].getWordListSize(); index += 1 )
                if( database[0].getEntry( index ).buildInternalDefinitionDatabase( context ) )
                    counter += 1;

            return Integer.toString( counter ) + " new definitions fetched and saved.";
        } // end method doInBackground

        protected void onPostExecute( String result )
        {
            Log.d( logTag, result );
        } // end method onPosteExecute

    } // end class AsyncDefinitionReader

} // end class WordListDatabase
