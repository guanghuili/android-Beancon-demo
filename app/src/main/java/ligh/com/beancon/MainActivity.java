package ligh.com.beancon;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.widget.TextView;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;

public class MainActivity extends Activity implements BeaconConsumer
{
	protected static final String TAG = "MonitoringActivity";
	/** 重新调整格式*/
	public static final String IBEACON_FORMAT = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24";
	/** 设置兴趣UUID*/
	public static final String FILTER_UUID = "206A2476-D4DB-42F0-BF73-030236F2C756";

	public TextView	textView;

	private BeaconManager beaconManager;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		textView = (TextView) findViewById(R.id.textView);

		textView.setText("Scanning...");

		beaconManager = BeaconManager.getInstanceForApplication(this);
		beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(IBEACON_FORMAT));
		beaconManager.bind(this);




    }

	@Override
	protected void onDestroy() {
		super.onDestroy();
		beaconManager.unbind(this);
	}


	Handler handler = new Handler()
	{
		@Override
		public void handleMessage(Message msg) {
		//	super.handleMessage(msg);

			Collection<Beacon> beacons = (Collection<Beacon>) msg.obj;

			textView.setText("Scanning...");

			if (beacons.size() > 0) {

				//Log.i(TAG, "The first beacon I see is about " + beacons.iterator().next().getServiceUuid() + " meters away.");

				textView.setText( "一个设备被扫描到 ID为： " + beacons.iterator().next().getId1()+
						" id2:"+beacons.iterator().next().getId2() +" id3 :"+beacons.iterator().next().getId3());
			}

		}
	};


	public void showNotification(String tickertext,String title,String content)
    {

        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification notification=new Notification(R.drawable.notification_template_icon_bg,tickertext,System.currentTimeMillis());
        notification.defaults= Notification.DEFAULT_ALL;


        Intent intent = new Intent(this,MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent, 1);
        notification.setLatestEventInfo(this,title,content,pendingIntent);
        nm.notify(2,notification);

    }


	@Override
	public void onBeaconServiceConnect()
	{

		beaconManager.setMonitorNotifier(new MonitorNotifier()
		{
			@Override
			public void didEnterRegion(Region region)
			{
				Log.i(TAG, "I just saw an beacon for the first time!");

                showNotification("a", "我的小店", "欢迎进入我的小店");
			}

			@Override
			public void didExitRegion(Region region)
			{
				Log.i(TAG, "I no longer see an beacon");
                showNotification("a", "我的小店","欢迎你下次再来");
			}

			@Override
			public void didDetermineStateForRegion(int state, Region region)
			{
				Log.i(TAG, "I have just switched from seeing/not seeing beacons: "+state);
			}
		});

		try {
			beaconManager.startMonitoringBeaconsInRegion(new Region("myMonitoringUniqueId", null, null, null));
		} catch (RemoteException e)
		{

		}


		//范围检测
		beaconManager.setRangeNotifier(new RangeNotifier() {
			@Override
			public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {

				Message message = new Message();
				message.obj = beacons;
				handler.sendMessage(message);

				Log.i(TAG,"Scanning....");

				if (beacons.size() > 0) {

					Log.i(TAG, "The first beacon I see is about " + beacons.iterator().next().getBluetoothName()+ " meters away.");

				}
			}
		});



		try {
			beaconManager.startRangingBeaconsInRegion(new Region(FILTER_UUID, null, null, null));
		} catch (RemoteException e) {    }



	}

}
