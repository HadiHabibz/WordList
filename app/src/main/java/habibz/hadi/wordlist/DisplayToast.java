package habibz.hadi.wordlist;

import android.content.Context;
import android.widget.Toast;

public final class DisplayToast
{

    public static void show( final String message,
                             final Context context,
                             final String duration )
    {
        if( duration.equals( "long" ) )
            Toast.makeText( context, message, Toast.LENGTH_LONG ).show();

        else
            Toast.makeText( context, message, Toast.LENGTH_SHORT ).show();

    } // end method show

} // end class DispalyToast
