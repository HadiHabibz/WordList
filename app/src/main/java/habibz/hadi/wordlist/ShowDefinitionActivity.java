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
        // It will get corrected automatically
        String dummyWordLine = word + "(v)";
        WordEntity wordEntity = new WordEntity( dummyWordLine );
        wordEntity.parseJson( this );
        showDefinition( wordEntity );
    } // end method onCreate

    private void showDefinition( WordEntity wordEntity )
    {
        int index = 0;

        recyclerView = ( RecyclerView ) findViewById( R.id.recyclerViewDefinition );
        recyclerView.setHasFixedSize( true );
        layoutManager = new LinearLayoutManager( getApplicationContext() );
        recyclerView.setLayoutManager( layoutManager );
        adapter = new ShowDefinitionActivity.DefinitionAdapter( wordEntity );
        recyclerView.setAdapter( adapter );

    } // end method parseJson

    public class DefinitionAdapter
            extends RecyclerView.Adapter< ShowDefinitionActivity.definitionViewHolder >
    {
        private WordEntity wordEntity;

        public DefinitionAdapter( WordEntity wordEntity )
        {
            this.wordEntity = wordEntity;
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
            Vector<Definition> allDefinitions = wordEntity.getDefinitions();

            String partOfSpeech = allDefinitions.elementAt( position ).getPartOfSpeech();
            String definition = allDefinitions.elementAt( position ).getDefinition();
            String stems = allDefinitions.elementAt( position ).getStems();
            String phonetics = "\\" + allDefinitions.elementAt( position ).getPhonetics() + "\\";
            holder.partOfSpeechTextView.setText( partOfSpeech );

            holder.definitionTextView.setText( Html.fromHtml( definition,
                    Html.FROM_HTML_SEPARATOR_LINE_BREAK_PARAGRAPH ) );

            holder.stemsTextView.setText( stems );
            holder.phoneticsTextView.setText( phonetics );
        } // end method onBindViewHolder

        @Override
        public int getItemCount()
        {
            return wordEntity.getDefinitions().size();
        }

    } // end class wordListAdapter

    public class definitionViewHolder extends RecyclerView.ViewHolder
    {
        public TextView partOfSpeechTextView;
        public TextView definitionTextView;
        public TextView stemsTextView;
        public TextView phoneticsTextView;

        public definitionViewHolder( View view )
        {
            super( view );
            this.partOfSpeechTextView = view.findViewById( R.id.definitionTextView_partOfSpeech );
            this.definitionTextView = view.findViewById( R.id.definitionTextView_definition );
            this.stemsTextView = view.findViewById( R.id.definitionTextView_stems );
            this.phoneticsTextView = view.findViewById( R.id.definitionTextView_phonetics );
        } // end constructor

    } // end class WordViewHolder

} // end class ShowDefinitionActivity
