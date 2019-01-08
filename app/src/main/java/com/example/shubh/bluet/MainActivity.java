package com.example.shubh.bluet;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{

    BluetoothAdapter m;
    Button b,b2,b3,b4;
    Button btnconn,btnsend;
    bcservice mser;
    Context mContext;
    BluetoothDevice mBTDevice;
    EditText etSend;
    private static final UUID myuuid =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    ArrayList<BluetoothDevice> bdv=new ArrayList<>();
    public dla df;
    ListView lv;
    TextView t1,t2;
    TextView incomingMessages;
    StringBuilder messages;



    private final BroadcastReceiver m1=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String g=intent.getAction();
            if(g.equals(m.ACTION_STATE_CHANGED))
            {
                final int st=intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,m.ERROR);

                switch (st)
                {
                    case BluetoothAdapter.STATE_OFF:
                    Log.d(" qqqq","off");
                    break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(" qqqq"," tur off");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d("qqqq ","on");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d("qqqq ","tur on");
                        break;

                }
            }
        }
    };

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {

                int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);

                switch (mode) {
                    //Device is in Discoverable Mode
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d("a", "mBroadcastReceiver2: Discoverability Enabled.");
                        break;
                    //Device not in discoverable mode
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d("a", "mBroadcastReceiver2: Discoverability Disabled. Able to receive connections.");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d("a", "mBroadcastReceiver2: Discoverability Disabled. Not able to receive connections.");
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.d("a", "mBroadcastReceiver2: Connecting....");
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.d("a", "mBroadcastReceiver2: Connected.");
                        break;
                }

            }
        }
    };
    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver mReceiver2 = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                bdv.add(device);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                Log.d("search",deviceName+" "+deviceHardwareAddress);
              /*  t1.setText(deviceName);
                t2.setText(deviceHardwareAddress);*/

                df=new dla(context,R.layout.dav,bdv);
               // lv.deferNotifyDataSetChanged();
                lv.setAdapter(df);
            }
        }
    };

    private final BroadcastReceiver m4=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String a=intent.getAction();

            if(BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(a)){
                BluetoothDevice mbd= intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(mbd.getBondState()==BluetoothDevice.BOND_BONDED)
                {
                    Log.d("qwqwwqw","bondeddddddddddddd");
                    mBTDevice=mbd;
                }
                if(mbd.getBondState()==BluetoothDevice.BOND_BONDING)
                {
                    Log.d("qwwq","bonding");
                }
                if(mbd.getBondState()==BluetoothDevice.BOND_NONE)
                {
                    Log.d("qwwq","none");
                }
            }
        }
    };

    @Override
    protected void onDestroy(){
        super.onDestroy();
        unregisterReceiver(m1);
        unregisterReceiver(mReceiver);
        unregisterReceiver(mReceiver2);
        unregisterReceiver(m4);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        b=(Button) findViewById(R.id.button);
        b2=(Button) findViewById(R.id.button2);
        b3=(Button)findViewById(R.id.button3);
        m= BluetoothAdapter.getDefaultAdapter();
        lv=(ListView)findViewById(R.id.ynamic);
        bdv=new ArrayList<>();
       t1=(TextView)findViewById(R.id.textView) ;
        t2=(TextView)findViewById(R.id.textView1);
        lv.setOnItemClickListener(MainActivity.this);
        b4=(Button)findViewById(R.id.bchat);
        btnconn=(Button)findViewById(R.id.button5);
        btnsend=(Button)findViewById(R.id.button6);
        etSend = (EditText) findViewById(R.id.editText);
        incomingMessages=(TextView)findViewById(R.id.textView2);
        messages=new StringBuilder();

        LocalBroadcastManager.getInstance(this).registerReceiver(mReciever,new IntentFilter("incomingMessage"));

        btnconn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startConnection();
            }
        });
        btnsend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte[] bytes = etSend.getText().toString().getBytes(Charset.defaultCharset());
                mser.write(bytes);
            }
        });



        b4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(MainActivity.this,chatwall.class);
            }
        });

        IntentFilter bond=new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(m4,bond);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eb();
            }
        });

        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disc();
            }
        });

        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search();
            }
        });
    }

    public void startBTconnection(BluetoothDevice device,UUID uuid){
        mser.startClient(device,uuid);

    }

    public void startConnection()
    {
        startBTconnection(mBTDevice,myuuid);
    }


    BroadcastReceiver mReciever=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String text=intent.getStringExtra("theMessage");
            messages.append(text+"\n");
            incomingMessages.setText(messages);
        }
    };

    public void eb()
    {
        if(!m.isEnabled())
        {
            Intent i=new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(i);
            IntentFilter j =new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(m1,j);

       }
       else
        {
            m.disable();
            IntentFilter j =new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(m1,j);
        }
    }

    public void disc()
    {
        Intent dis=new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        dis.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,300);
        startActivity(dis);
     IntentFilter it=new IntentFilter(m.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(mReceiver,it);

    }

    public void search()
    {
        Log.d("MainActivity","searching");
        if(m.isDiscovering()) {
            m.cancelDiscovery();
        }

        check();
            m.startDiscovery();
            IntentFilter sch = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mReceiver2, sch);

    }

    public void check() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permissionCheck != 0) {

                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
            }
        } else {
            Log.d("mainaaaaaa", "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        m.cancelDiscovery();

        String nam=bdv.get(position).getName();
        String ad=bdv.get(position).getAddress();
        bdv.get(position).createBond();
        mBTDevice=bdv.get(position);
        mser=new bcservice(MainActivity.this);

    }
}
