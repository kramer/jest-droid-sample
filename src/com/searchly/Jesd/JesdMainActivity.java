package com.searchly.Jesd;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.searchly.jestdroid.DroidClientConfig;
import com.searchly.jestdroid.JestClientFactory;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.indices.CreateIndex;

public class JesdMainActivity extends Activity {

    final Context context = this;
    private JestClient jestClient;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        requestApiKey();

        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new ConnectButtonListener());
    }

    private void simpleAlert(String title, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.show();
    }

    private void requestApiKey() {
        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.prompts, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setView(promptsView);

        ((TextView) promptsView.findViewById(R.id.textView1)).setText("Enter your Searchly.com API key:");

        final EditText userInput = (EditText) promptsView.findViewById(R.id.editTextDialogUserInput);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // init jest droid client
                                DroidClientConfig clientConfig = new DroidClientConfig.Builder("http://site:" + userInput.getText().toString() + "@eu-west-1.searchbox.io").build();

                                JestClientFactory jestClientFactory = new JestClientFactory();
                                jestClientFactory.setDroidClientConfig(clientConfig);
                                jestClient = jestClientFactory.getObject();
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Toast.makeText(JesdMainActivity.this, "Cannot continue without a Searchly.com API Key :(", Toast.LENGTH_LONG).show();
                                finish();
                                System.exit(0);
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    public class ConnectButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            // get prompts.xml view
            LayoutInflater li = LayoutInflater.from(context);
            View promptsView = li.inflate(R.layout.prompts, null);

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
            alertDialogBuilder.setView(promptsView);

            ((TextView) promptsView.findViewById(R.id.textView1)).setText("Name for the new index:");

            final EditText userInput = (EditText) promptsView.findViewById(R.id.editTextDialogUserInput);

            // set dialog message
            alertDialogBuilder
                    .setCancelable(false)
                    .setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    new CreateIndexTask().execute(userInput.getText().toString());
                                }
                            })
                    .setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });

            // create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();

            // show it
            alertDialog.show();
        }
    }

    class CreateIndexTask extends AsyncTask<String, Void, JestResult> {

        private Exception exception;
        private JestResult result;

        protected JestResult doInBackground(String... indexName) {
            try {
                result = jestClient.execute(new CreateIndex.Builder(indexName[0]).build());
                return result;
            } catch (Exception e) {
                this.exception = e;
                return null;
            }
        }

        protected void onPostExecute(JestResult feed) {
            if (exception == null) {
                if (result.isSucceeded()) {
                    simpleAlert("Created index", result.getJsonString());
                } else {
                    simpleAlert("Could not create index", result.getJsonString());
                }
            } else {
                simpleAlert("Exception occurred", exception.getMessage());
            }
        }
    }

}
