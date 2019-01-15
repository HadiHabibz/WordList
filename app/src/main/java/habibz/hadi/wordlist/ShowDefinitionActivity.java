package habibz.hadi.wordlist;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.JsonReader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import org.json.*;

import java.util.Vector;

public class ShowDefinitionActivity extends AppCompatActivity
{
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView.Adapter adapter;
    private static final String logTag = "ShowDefLogTag";

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setTitle( "Definition" );
        setContentView( R.layout.activity_show_definition );

        Intent intent = getIntent();
        String word = intent.getStringExtra( "wordDefinitionKey" );

        // Part of speech does not matter here
        String dummyWordLine = word + "(v)";
        WordEntity wordEntity = new WordEntity( dummyWordLine );
        String jsonContent = wordEntity.getJsonString( this );
        parseJson( jsonContent );
    } // end method onCreate

    private void parseJson( String jsonContentString )
    {
        int index = 0;

        Vector<Definition> definitions = new Vector<Definition>();

        jsonContentString =
                jsonContentString.substring( 1, jsonContentString.length()-2 );

        String[] allHomographs = getHomographString( jsonContentString );

        for( int i = 0; i < allHomographs.length; i++ )
        //for( int i = 0; i < 1; i++ )
        {
            definitions.add( new Definition( allHomographs[i] ) );
        } // end for

        recyclerView = ( RecyclerView ) findViewById( R.id.recyclerViewDefinition );
        recyclerView.setHasFixedSize( true );
        layoutManager = new LinearLayoutManager( getApplicationContext() );
        recyclerView.setLayoutManager( layoutManager );
        adapter = new ShowDefinitionActivity.DefinitionAdapter( definitions );
        recyclerView.setAdapter( adapter );

    } // end method parseJson

    public String[] getHomographString( final String json )
    {
        String[] splits = json.split( "\"meta\"" );

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

    private class Definition
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
                phonetics.add( "blank" );
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
                partOfSpeech.add( "blank" );
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
                phonetics = phonetics + this.phonetics.elementAt( i ) + "\n";

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

    public class DefinitionAdapter
            extends RecyclerView.Adapter< ShowDefinitionActivity.definitionViewHolder >
    {
        private Vector<Definition> allDefinitions;

        public DefinitionAdapter( Vector<Definition> allDefinitions )
        {
            this.allDefinitions = allDefinitions;
        } // end constructor

        @Override
        public ShowDefinitionActivity.definitionViewHolder
        onCreateViewHolder( ViewGroup parent, int viewType )
        {
            // create a new view
            View view = LayoutInflater.from( parent.getContext() )
                    .inflate( R.layout.definition_entry_view, parent, false );

            return new ShowDefinitionActivity.definitionViewHolder( view );
        } // end method onCreateViewHolder

        @Override
        public void onBindViewHolder(
                ShowDefinitionActivity.definitionViewHolder holder, int position )
        {
            String partOfSpeech = allDefinitions.elementAt( position ).getPartOfSpeech();
            String definition = allDefinitions.elementAt( position ).getDefinition();
            String stems = allDefinitions.elementAt( position ).getStems();
            holder.partOfSpeechTextView.setText( partOfSpeech );
            //holder.definitionTextView.setText( definition );

            holder.definitionTextView.setText( Html.fromHtml( definition,
                    Html.FROM_HTML_SEPARATOR_LINE_BREAK_PARAGRAPH ) );

            holder.stemsTextView.setText( stems );
        } // end method onBindViewHolder

        @Override
        public int getItemCount()
        {
            return allDefinitions.size();
        }

    } // end class wordListAdapter

    public class definitionViewHolder extends RecyclerView.ViewHolder
    {
        public TextView partOfSpeechTextView;
        public TextView definitionTextView;
        public TextView stemsTextView;

        public definitionViewHolder( View view )
        {
            super( view );
            this.partOfSpeechTextView = view.findViewById( R.id.definitionTextView_partOfSpeech );
            this.definitionTextView = view.findViewById( R.id.definitionTextView_definition );
            this.stemsTextView = view.findViewById( R.id.definitionTextView_stems );
        } // end constructor

    } // end class WordViewHolder

} // end class ShowDefinitionActivity
