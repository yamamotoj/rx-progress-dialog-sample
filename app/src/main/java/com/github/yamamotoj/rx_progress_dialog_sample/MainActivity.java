package com.github.yamamotoj.rx_progress_dialog_sample;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Date;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private TextView textView;
    private Subscription loadDataSubscription;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        textView = (TextView) findViewById(R.id.text_view);
        Button button1 = (Button) findViewById(R.id.button1);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClick1();
            }
        });
        Button button2 = (Button) findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClick2();
            }
        });
        Button button3 = (Button) findViewById(R.id.button3);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClick3();
            }
        });
        Button button4 = (Button) findViewById(R.id.button4);
        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClick4();
            }
        });

    }

    private void onClick1() {
        progressDialog = createProgressDialog();
        progressDialog.show();
        loadDataSubscription = loadData().subscribe(new Subscriber<String>() {
            @Override
            public void onCompleted() {
                progressDialog.dismiss();
            }

            @Override
            public void onError(Throwable e) {
                progressDialog.dismiss();
            }

            @Override
            public void onNext(String s) {
                textView.setText(s);
            }
        });
    }

    private void cancel() {
        if (loadDataSubscription != null && !loadDataSubscription.isUnsubscribed()) {
            loadDataSubscription.unsubscribe();
            progressDialog.dismiss();
        }
    }

    private void onClick2() {
        final ProgressDialog progressDialog = createProgressDialog();
        loadData()
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        progressDialog.show();
                    }
                })
                .doOnUnsubscribe(new Action0() {
                    @Override
                    public void call() {
                        progressDialog.dismiss();
                    }
                })
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        textView.setText(s);
                    }
                });
    }

    private void onClick3() {
        Observable.using(
                new Func0<ProgressDialog>() {
                    @Override
                    public ProgressDialog call() {
                        ProgressDialog progressDialog = createProgressDialog();
                        progressDialog.show();
                        return progressDialog;
                    }
                },
                new Func1<ProgressDialog, Observable<? extends String>>() {
                    @Override
                    public Observable<? extends String> call(ProgressDialog progressDialog) {
                        return loadData();
                    }
                },
                new Action1<ProgressDialog>() {
                    @Override
                    public void call(ProgressDialog progressDialog) {
                        progressDialog.dismiss();
                    }
                })
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        textView.setText(s);
                    }
                });
    }


    private void onClick4() {
        usingProgressDialog()
                .flatMap(new Func1<Void, Observable<String>>() {
                    @Override
                    public Observable<String> call(Void aVoid) {
                        return loadData();
                    }
                })
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        textView.setText(s);
                    }
                });
    }

    private Observable<Void> usingProgressDialog() {
        return Observable.using(
                new Func0<ProgressDialog>() {
                    @Override
                    public ProgressDialog call() {
                        ProgressDialog progressDialog = createProgressDialog();
                        progressDialog.show();
                        return progressDialog;
                    }
                },
                new Func1<ProgressDialog, Observable<? extends Void>>() {
                    @Override
                    public Observable<? extends Void> call(ProgressDialog progressDialog) {
                        return Observable.just(null);
                    }
                },
                new Action1<ProgressDialog>() {
                    @Override
                    public void call(ProgressDialog progressDialog) {
                        progressDialog.dismiss();
                    }
                });
    }


    private ProgressDialog createProgressDialog() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        return progressDialog;
    }


    private Observable<String> loadData() {
        // wait 3 seconds in background thread
        // then strings are observed on main thread
        return Observable
                .create(new Observable.OnSubscribe<String>() {
                    @Override
                    public void call(Subscriber<? super String> subscriber) {
                        SystemClock.sleep(3000);
                        subscriber.onNext(new Date().toString());
                        subscriber.onCompleted();
                    }
                })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
