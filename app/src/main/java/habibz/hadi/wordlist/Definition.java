package habibz.hadi.wordlist;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Vector;

public class Definition
{
    private String jsonString;
    private Vector<String> stems;
    private Vector<String> phonetics;
    private Vector<String> partOfSpeech;
    private Vector<String> meaning;
    private static final String logTag = "DefinitionLogTag";

    public Definition( final String jsonString )
    {
        this.stems = new Vector<String>();
        this.phonetics = new Vector<String>();
        this.partOfSpeech = new Vector<String>();
        this.meaning = new Vector<String>();
        this.jsonString = jsonString;

        this.setStem();
        this.setPhonetics();
        this.setPartOfSpeech();
        this.setMeaning();
    } // end Constructor

    public Definition()
    {
        this.stems = new Vector<String>();
        this.phonetics = new Vector<String>();
        this.partOfSpeech = new Vector<String>();
        this.meaning = new Vector<String>();
        this.stems.add( "" );
        this.phonetics.add( "" );
        this.meaning.add( "no meaning" );
        this.partOfSpeech.add( "" );
    } // end Constructor


    private void setStem()
    {
        try
        {
            JSONObject object = new JSONObject( jsonString );
            String metaPortion = object.getString( "meta" );
            JSONObject objectNested = new JSONObject( metaPortion );
            JSONArray array = objectNested.getJSONArray( "stems" );

            for( int i = 0; i < array.length(); i++ )
                stems.add( array.getString( i ) );

            //Log.d( logTag, getStems() );
        }
        catch( JSONException e )
        {
            Log.d( logTag, "Cannot parse json." );
            Log.d( logTag, e.getMessage() );
            stems.add( "" );
        }

    } // end method setStem

    private void setPhonetics()
    {
        try
        {
            JSONObject object = new JSONObject( jsonString );
            String headerPortion = object.getString( "hwi" );
            JSONObject objectNested = new JSONObject( headerPortion );
            JSONArray array = objectNested.getJSONArray( "prs" );
            objectNested = new JSONObject( array.getString( 0 ) );
            String fonetics = objectNested.getString( "mw" );
            phonetics.add( fonetics );
            // Log.d( logTag, fonetics );
        }

        catch( JSONException e )
        {
            Log.d( logTag, "Cannot parse json." );
            Log.d( logTag, e.getMessage() );
            phonetics.add( " " );
        }
    } // end method setPhonetics

    private void setPartOfSpeech()
    {
        try
        {
            JSONObject object = new JSONObject( jsonString );
            String headerPortion = object.getString( "fl" );
            partOfSpeech.add( headerPortion );
            Log.d( logTag, headerPortion );
        }

        catch( JSONException e )
        {
            Log.d( logTag, "Cannot parse json." );
            Log.d( logTag, e.getMessage() );
            partOfSpeech.add( " " );
        }

    } // end method setPartOfSpeech

    private void setMeaning()
    {
        try
        {
            JSONObject object = new JSONObject( jsonString );
            JSONArray array = object.getJSONArray( "def" );
            String nestedString = array.getString( 0 );
            JSONObject nestedObject = new JSONObject( nestedString );
            JSONArray senseSequence = nestedObject.getJSONArray( "sseq" );

            for( int i = 0; i < senseSequence.length(); i++ )
                processSenseSeq( senseSequence );
        }
        catch( JSONException e )
        {
            Log.d( logTag, "Cannot parse json (meaning)." );
            Log.d( logTag, e.getMessage() );
            meaning.add( "" );
        };
    } // end method setMeaning

    private void processSenseSeq( final JSONArray senseSequence )
    {

        try
        {
            String definition = "";

            for( int i = 0; i < senseSequence.length(); i++ )
            {
                String senseString = "";
                senseString = senseSequence.getJSONArray( i ).
                        getJSONArray(0).getString( 1 );
                JSONObject object = new JSONObject( senseString );

                try
                {
                    definition = definition + object.getString("sn");
                }
                catch(JSONException e )
                {
                    Log.d( logTag, "Cannot fetch sn -> " + e.getMessage() );
                }

                JSONArray array = object.getJSONArray( "dt" );
                senseString = array.getJSONArray( 0 ).getString( 1 );
                definition = definition + senseString + "\n";
                Log.d( logTag, definition );
            } // for i

            meaning.add( definition );
        }
        catch( JSONException e )
        {
            Log.d( logTag, "Cannot parse json (meaning 2)." );
            Log.d( logTag, e.getMessage() );
            meaning.add( "No Definition!" );
        };

    } // end method processSenseSeq

    public String getStems()
    {
        String stems = "";

        for( int i = 0; i < this.stems.size(); i++ )
            stems = stems + this.stems.elementAt( i ) + "\n";

        return stems;
    } // end method getStems

    public String getPhonetics()
    {
        String phonetics = "";

        for( int i = 0; i < this.phonetics.size(); i++ )
            phonetics = phonetics + this.phonetics.elementAt( i );

        return phonetics;
    } // end method getPhonetics

    public String getPartOfSpeech()
    {
        String partOfSpeech = "";

        for( int i = 0; i < this.partOfSpeech.size();i ++ )
            partOfSpeech = partOfSpeech + this.partOfSpeech.elementAt( i );

        return partOfSpeech;
    } // end method getPartOfSpeech

    public String getDefinition()
    {
        String definition = "";

        for( int i = 0; i < this.meaning.size(); i++ )
            definition = definition + this.meaning.elementAt( i );

        definition = removeStringFormat( definition );
        return definition;
    } // end method getDefinition

    private String removeStringFormat( String string )
    {
        string = string.replaceAll( "\n", "<br>" );
        string = string.replaceAll( "\\{bc\\}", "<b>: </b>" );
        string = string.replaceAll( "\\{it\\}", "<i>" );
        string = string.replaceAll( "\\{\\/it\\}", "</i>" );
        string = string.replaceAll( "\\{b\\}", "<b>" );
        string = string.replaceAll( "\\{\\/b\\}", "</b>" );
        string = string.replaceAll( "\\{sup\\}", "<sup>" );
        string = string.replaceAll( "\\{\\/sup\\}", "</sup>" );
        string = string.replaceAll( "\\{inf\\}", "<sub>" );
        string = string.replaceAll( "\\{\\/inf\\}", "</sub>" );
        string = string.replaceAll( "\\{sx", " " );
        string = string.replaceAll( "\\{d_link", " " );
        string = string.replaceAll( "\\{et_link", " " );
        string = string.replaceAll( "\\{mat", " " );
        string = string.replaceAll( "\\{dxt", " " );
        string = string.replaceAll( "\\{a_link", " " );
        string = string.replaceAll( "\\|", " " );
        string = string.replaceAll( "\\}", " " );

        return string;
    } // end method removeStringFormat

} // end class Definition
