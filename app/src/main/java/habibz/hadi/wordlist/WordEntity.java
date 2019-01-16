package habibz.hadi.wordlist;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Vector;

public class WordEntity
{
    // These three can also be fetched from definition
    // Yet, these three are read from files once, when
    // the program launchers, while the values in 'definitions'
    // are extracted live from json and if need be json itself
    // is extracted from the internet.
    private String word;
    private String partOfSpeech;
    private String phonetics;

    private Vector<Definition> definitions;

    private static final String logTag = "WordEntityLogTag";

    public WordEntity( final String line )
    {
        this.parseLine( line );
        definitions = new Vector<Definition>();
    } // end constructor

    // Check to see if the json definition file belonging to this word
    // already exists internally or not. If it does, then no further
    // action is required. If it does not, fetch the file from the
    // internet and save it locally for future use. Return true if and
    // update occurs.
    public boolean buildInternalDefinitionDatabase( final Context context )
    {

        if( definitionExistsInternal( context ) )
            return false;

        String jsonContent = fetchWordDefinitionFromAPI();
        saveJsonFile( context, jsonContent );
        return true;

    } // end method buildInternalDefinitionDatabase

    public boolean buildInternalPhoneticsDatabase( final Context context )
    {
        if( !definitionExistsInternal( context ) )
            return false;

        if( phoneticsExistsInternal( context ) )
            return false;

        this.parseJson( context );
        String phonetics = this.getDefinitions( 0 ).getPhonetics();

        if( phonetics.equals( "" ) )
            return false;

        savePhoneticsFile( context, phonetics );
        return true;
    } // end method buildInternalPhoneticsDatabase

    public String getWord()
    {
        return this.word;
    } // end method getWord

    private void setPhonetics( final Context context )
    {
        String phonetics = this.fetchPhoneticsFromInternalFile( context );

        // Just for cosmetic purposes, show the actual word
        if( phonetics.equals( "" ) )
            this.phonetics = this.word;

        else
            this.phonetics = phonetics;

    } // end method setPhonetics

    public String getPhonetics( final Context context )
    {
        setPhonetics( context );
        return this.phonetics;
    } // end method getPhonetics

    public String getPartOfSpeech()
    {
        return this.partOfSpeech;
    } // end method getPartOfSpeech

    public Vector<Definition> getDefinitions()
    {
        return definitions;
    } // end method getDefinitions

    public Definition getDefinitions( final int position )
    {
        return definitions.elementAt( position );
    } // end method getDefinitions

    public void parseJson( final Context context )
    {
        String  jsonContentString = this.getJsonString( context );

        jsonContentString =
                jsonContentString.substring( 1, jsonContentString.length()-2 );

        String[] allHomographs = getHomographString( jsonContentString );

        if( allHomographs == null )
        {
            definitions.add( new Definition() );
            return;
        }

        for( int i = 0; i < allHomographs.length; i++ )
            definitions.add( new Definition( allHomographs[i] ) );

    } // end method parseJson

    // Return the entire JSON file associated with this
    // word as a string. If the word JSON file already
    // exists in the local directory, fetch from there.
    // Otherwise, return a dummy string.
    private String getJsonString( final Context context )
    {
        String jsonContent = "";

        if( definitionExistsInternal( context ) )
        {
            jsonContent = fetchWordDefinitionFromInternalFile(context);
            return jsonContent;
        }

        return "blank";
    } // end method getJsonString

    private String[] getHomographString( final String json )
    {
        String[] splits = json.split( "\"meta\"" );

        if( splits.length == 1 )
            return null;

        if( splits.length == 2 )
        {
            String[] singleHomograph = new String[1];
            singleHomograph[0] = json;
            return singleHomograph;
        }

        String[] allHomographs = new String[splits.length-1];

        int i = 0;
        for( i = 0; i < splits.length-2; i++ )
        {
            allHomographs[i] = "{\"meta\":{" +
                    splits[i+1].substring( 3, splits[i+1].length()-2 );
        }

        allHomographs[i] = "{\"meta\":{" +
                splits[i+1].substring( 3, splits[i+1].length() );

        return allHomographs;

    } // end method getHomographString

    // Check if the json file containing the definition of this word
    // is already saved in the internal storage or not. Return true if
    // it exists.
    private boolean definitionExistsInternal( final Context context )
    {
        String wordFilename = getWord() + "_def.json";
        String[] allFilenames = context.fileList();

        if( !Arrays.asList( allFilenames ).contains( wordFilename ) )
            return false;

        String fileContent = fetchWordDefinitionFromInternalFile( context );

        // short or corrupt file
        if( fileContent.length() < 50 )
            return false;

        return true;

    } // end method definitionExistsInternal

    // Check if the json file containing the definition of this word
    // is already saved in the internal storage or not. Return true if
    // it exists.
    private boolean phoneticsExistsInternal( final Context context )
    {
        String wordFilename = getWord() + "_phon.txt";
        String[] allFilenames = context.fileList();

        if( !Arrays.asList( allFilenames ).contains( wordFilename ) )
            return false;

        String fileContent = fetchWordDefinitionFromInternalFile( context );

        return true;

    } // end method definitionExistsInternal

    // Read the word from a line
    // Basically, extract the word and part of the speech
    // Each line in the file must have a format like this:
    // Warble (v, n)
    // The part of the speech must be in parentheses.
    // They can be separated by comma
    private void parseLine( final String line )
    {
        int index = 0;
        int wordEndIndex;

        while( true )
        {
            if( line.charAt( index ) == 0 )
                break;

            if( line.charAt( index ) == '(' )
                break;

            index = index + 1;
        } // end while

        wordEndIndex = index - 1;

        while( line.charAt( wordEndIndex ) == ' ' )
            wordEndIndex -= 1;

        this.word = line.substring( 0, wordEndIndex+1 );

        wordEndIndex = index + 1;
        index = index + 1;

        while( line.charAt( index ) != ')' )
            index += 1;

        this.partOfSpeech = line.substring( wordEndIndex, index );
    } // end method parseLine

    // Each word must have a unique URL for making the query
    // For this, a key is need to allow us access to Merriam Webster
    // dictionary. Here, I have hard codded my key. See this for
    // further information about constructing the URL.
    // https://dictionaryapi.com/products/api-collegiate-dictionary
    private String getURL()
    {
        String firstPart = "https://www.dictionaryapi.com/api/";
        String secondPart = "v3/references/collegiate/json/";
        String key = "?key=4624a213-b3b6-47d2-bf0b-6dbabac34789";
        return firstPart + secondPart + this.word + key;
    } // end getURL

    // Load the definition (the content of josn file associated with this word)
    // and return its content.
    private String fetchWordDefinitionFromInternalFile( final Context context )
    {
        final String filename = getWord() + "_def.json";
        String fileContent = "";
        String line;
        FileInputStream inputStream;

        try
        {
            inputStream = context.openFileInput( filename );
            InputStreamReader inputStreamReader = new InputStreamReader( inputStream );
            BufferedReader bufferedReader = new BufferedReader( inputStreamReader );
            StringBuilder stringBuffer = new StringBuilder();
            line = bufferedReader.readLine();

            while( line != null )
            {
                stringBuffer.append( line + "\n" );
                line = bufferedReader.readLine();
            }

            fileContent = stringBuffer.toString();

            bufferedReader.close();
            inputStreamReader.close();
            inputStream.close();

            return fileContent;
        }

        catch( FileNotFoundException e )
        {
            Log.d( logTag, "Cannot open definition file!" );
            Log.d( logTag, e.getMessage() );
            return fileContent;
        }

        catch( IOException e )
        {
            Log.d( logTag, "Cannot read definition file!" );
            Log.d( logTag, e.getMessage() );
            return fileContent;
        }
    } // end method readJsonFromLocalFile

    // Load the phonetics form internal storage
    private String fetchPhoneticsFromInternalFile( final Context context )
    {
        final String filename = getWord() + "_phon.txt";
        String fileContent = "";
        String line;
        FileInputStream inputStream;

        try
        {
            inputStream = context.openFileInput( filename );
            InputStreamReader inputStreamReader = new InputStreamReader( inputStream );
            BufferedReader bufferedReader = new BufferedReader( inputStreamReader );
            StringBuilder stringBuffer = new StringBuilder();
            line = bufferedReader.readLine();

            while( line != null )
            {
                stringBuffer.append( line );
                line = bufferedReader.readLine();
            }

            fileContent = stringBuffer.toString();

            bufferedReader.close();
            inputStreamReader.close();
            inputStream.close();

            return fileContent;
        }

        catch( FileNotFoundException e )
        {
            Log.d( logTag, "Cannot open phonetics file!" );
            Log.d( logTag, e.getMessage() );
            return fileContent;
        }

        catch( IOException e )
        {
            Log.d( logTag, "Cannot read phonetics file!" );
            Log.d( logTag, e.getMessage() );
            return fileContent;
        }
    } // end method fetchPhoneticsFromInternalFile

    // After fetching the definition from the internet
    // save the result in the internal storage to avoid
    // future API access. The files are saved with this
    // filename: word_def.json e.g., warble_def.json
    private void saveJsonFile( final Context context, final String jsonString )
    {
        String filename = getWord() + "_def.json";
        FileOutputStream outputStream;

        try
        {
            outputStream = context.openFileOutput( filename, Context.MODE_PRIVATE );
            outputStream.write( jsonString.getBytes() );
            outputStream.close();
        }

        catch( IOException e )
        {
            Log.d( logTag, "Could not save the definition." );
            Log.d( logTag, e.getMessage() );
        }

    } // end method saveJsonFile

    // After fetching the phonetics form the internet
    // save the results in a file. This helps avoid
    // parsing the json file in the word list to fetch
    // the definition, improving performance
    private void savePhoneticsFile( final Context context, final String phonetics )
    {
        String filename = getWord() + "_phon.txt";
        FileOutputStream outputStream;

        try
        {
            outputStream = context.openFileOutput( filename, Context.MODE_PRIVATE );
            outputStream.write( phonetics.getBytes() );
            outputStream.close();
        }

        catch( IOException e )
        {
            Log.d( logTag, "Could not save the phonetics." );
            Log.d( logTag, e.getMessage() );
        }

    } // end method saveJsonFile

    // If the json file containing the definition of the
    // word does not exists, this method fetches it from
    // the internet (Merriam-Webster dictionary). This
    // function cannot be called from the main UI thread.
    // (considering calling it from AsyncTask). Also, for
    // this to work, internet access permission should be
    // added to the manifest file. Also remember that this
    // only works with a valid key from Merriam-Webster.
    private String fetchWordDefinitionFromAPI()
    {
        String constructedURL;
        String jsonText = "";
        InputStream inputStream;
        BufferedReader reader;

        // Get URL unique to this word
        constructedURL = this.getURL();

        try
        {
            inputStream = new URL( constructedURL ).openStream();

            reader = new BufferedReader(  new InputStreamReader(
                    inputStream, Charset.forName( "UTF-8" ) ) );

            jsonText = this.readJsonFromBufferedReader( reader );

            reader.close();
            inputStream.close();

            return jsonText;
        }

        catch( MalformedURLException e )
        {
            Log.d( logTag, "Dictionary APU: Not a valid URL" );
            Log.d( logTag, e.getMessage() );
            return jsonText;
        }

        catch( IOException e )
        {
            Log.d( logTag, "Dictionary API: Cannot open input stream." );
            Log.d( logTag, e.getMessage() );
            return jsonText;
        }

    } // end method fetchWordDefinitionFromAPI

    // Read the content of the json file from the Internet
    // and save it as a string. This does not do any Json
    // parsing. It merely returns the entire thing.
    private String readJsonFromBufferedReader( Reader reader )
            throws IOException
    {
        StringBuilder stringBuilder = new StringBuilder();

        int cp;

        while ((cp = reader.read()) != -1)
           stringBuilder.append((char) cp);

        return stringBuilder.toString();

    } // end method readJsonFromBufferedReader

} // end class WordEntity
