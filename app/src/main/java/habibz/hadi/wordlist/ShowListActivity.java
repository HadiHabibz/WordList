package habibz.hadi.wordlist;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import org.w3c.dom.Text;

public class ShowListActivity extends AppCompatActivity
{
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView.Adapter adapter;
    private WordListDatabase wordList;
    private Intent intent;
    private static final String logTag = "ShowListActivityLogTag";

    public interface OnItemClickListener
    {
        public void onItemClick( View view, int position );
    }

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_show_list );
        intent = new Intent( this, ShowDefinitionActivity.class );
        wordList = new WordListDatabase( getApplicationContext() );
        recyclerView = ( RecyclerView ) findViewById( R.id.recyclerView );
        recyclerView.setHasFixedSize( true );
        layoutManager = new LinearLayoutManager( getApplicationContext() );
        recyclerView.setLayoutManager( layoutManager );
        adapter = new WordListAdapter( wordList, this );
        recyclerView.setAdapter( adapter );

        recyclerView.addItemDecoration(
                new DividerItemDecoration(
                        getApplicationContext(), LinearLayoutManager.VERTICAL ) );

        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener( this,
                        new OnItemClickListener() {
                    @Override public void onItemClick( View view, int position )
                    {
                        launchDefinitionActivity( wordList.getEntry( position ) );
                    }
                })
        );

    } // end method onCreate

    public void launchDefinitionActivity( final WordEntity word )
    {
        intent.putExtra( "wordDefinitionKey", word.getWord() );
        startActivity( intent );
    } // end method launchDefinitionActivity

    public class WordListAdapter extends RecyclerView.Adapter< ShowListActivity.WordViewHolder >
    {
        private WordListDatabase wordList;
        private Context context;

        public WordListAdapter( WordListDatabase wordList, final Context context )
        {
            this.wordList = wordList;
            this.context = context;
        } // end constructor

        @Override
        public ShowListActivity.WordViewHolder onCreateViewHolder( ViewGroup parent, int viewType )
        {
            // create a new view
            View view = LayoutInflater.from( parent.getContext() )
                    .inflate( R.layout.word_entry_view, parent, false );

            return new WordViewHolder( view );
        } // end method onCreateViewHolder

        @Override
        public void onBindViewHolder( WordViewHolder holder, int position )
        {
            String word = wordList.getEntry( position ).getWord();
            String partOfSpeech = "("+ wordList.getEntry( position ).getPartOfSpeech() + ")";
            String reservedPart = "*";
            String phonetics = "\\" + wordList.getEntry( position ).getPhonetics( context ) + "\\";
            holder.wordTextView.setText( word );
            holder.partOfSpeechTextView.setText( partOfSpeech );
            holder.reservedTextView.setText( reservedPart );
            holder.phoneticsTextView.setText( phonetics );
        } // end method onBindViewHolder

        @Override
        public int getItemCount()
        {
            return wordList.getWordListSize();
        }

    } // end class wordListAdapter

    public class WordViewHolder extends RecyclerView.ViewHolder
    {
        public TextView wordTextView;
        public TextView partOfSpeechTextView;
        public TextView reservedTextView;
        public TextView phoneticsTextView;

        public WordViewHolder( View view )
        {
            super( view );
            this.wordTextView = view.findViewById( R.id.wordview );
            this.partOfSpeechTextView = view.findViewById( R.id.partofspeechview );
            this.reservedTextView = view.findViewById( R.id.reservedview );
            this.phoneticsTextView = view.findViewById( R.id.phonetextview );
        } // end constructor

    } // end class WordViewHolder

    public class RecyclerItemClickListener implements RecyclerView.OnItemTouchListener
    {
        private OnItemClickListener clickListener;
        public GestureDetector gestureDetector;

        public RecyclerItemClickListener( Context context, OnItemClickListener listener )
        {
            clickListener = listener;

            gestureDetector = new GestureDetector(
                    context, new GestureDetector.SimpleOnGestureListener()
            {
                @Override
                public boolean onSingleTapUp( MotionEvent e )
                {
                    return true;
                }

            } );
        } // end method RecyclerItemClickListener

        @Override
        public boolean onInterceptTouchEvent( RecyclerView view, MotionEvent e )
        {
            View childView = view.findChildViewUnder( e.getX(), e.getY() );

            if ( childView != null && clickListener != null && gestureDetector.onTouchEvent( e ) )
            {
                clickListener.onItemClick( childView, view.getChildAdapterPosition( childView ) );
                return true;
            }

            return false;
        } // end method onInterceptTouchEvent

        @Override
        public void onTouchEvent( RecyclerView view, MotionEvent motionEvent )
        {
            // empty
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent( boolean b )
        {
            // empty
        }

    } // end class RecyclerItemClickListener

} // end class ShowListActivity

