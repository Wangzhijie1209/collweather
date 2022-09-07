package com.coolweather.android;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.coolweather.android.db.City;
import com.coolweather.android.db.County;
import com.coolweather.android.db.Province;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;

import org.litepal.LitePal;
import org.litepal.crud.LitePalSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChooseAreaFragment extends Fragment {

    private ProgressDialog progressDialog;
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;
    private List<String> dataList = new ArrayList<>();
    /**
     * 省列表
     */
    private List<Province> provinceList;

    /**
     * 市列表
     */
    private List<City> cityList;

    /**
     * 县列表
     */
    private List<County> countyList;

    /**
     * 选中的省份
     */
    private Province selectedProvince;

    /**
     * 选中的城市
     */
    private City selectedCity;

    /**
     * 当前选中的级别
     */
    private int currentLevel;
    private TextView titleText;
    private Button backButton;
    private ListView listView;
    private ArrayAdapter<String> adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area, container, false);
        titleText = view.findViewById(R.id.title_text);
        backButton = view.findViewById(R.id.back_button);
        listView = view.findViewById(R.id.list_view);
        //初始化arrayAdapter
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, dataList);
        //设置为ListView的适配器
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            //当你点击了某个省的时候会进入到ListView的onItemClick()方法中,这个时候会根据当前的级别来判断是去调用
            //queryCities()方法还是queryCounties()方法,queryCities()方法是去查询市级数据,而queryCounties()
            //方法是去查询县级数据,这两个方法内部流程和queryProvinces()方法基本相同
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (currentLevel == LEVEL_PROVINCE) {
                    selectedProvince = provinceList.get(position);
                    queryCities();
                }else if(currentLevel == LEVEL_CITY){
                    selectedCity = cityList.get(position);
                    queryCounties();
                }
            }
        });
        //在返回按钮的点击事件里,会对当前ListView的列表级别进行判断
        //如果当前是县级别,那么就会返回到市级列表,如果当前是市级列表那么就会返回到省级列表
        //当返回到省级列表时,返回按钮就会自动隐藏,从而也就不需要再做进一步的处理了
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currentLevel == LEVEL_COUNTY){
                    queryCities();
                }else if(currentLevel == LEVEL_CITY){
                    queryProvinces();
                }
            }
        });
        //加载省级数据
        queryProvinces();
    }

    /**
     * 查询全国所有的省,优先从数据库查询,如果没有查询到再去服务器上查询
     */
    private void queryProvinces() {
        //将头布局标题设置成中国
        titleText.setText("中国");
        //将返回按钮隐藏起来,因为省级列表已经不能返回了
        backButton.setVisibility(View.GONE);
        //调用LitePal的查询结构从数据库中读取省级数据
        provinceList = LitePal.findAll(Province.class);
        //如果读取到了就直接将数据显示到界面上,
        if (provinceList.size() > 0) {
            dataList.clear();
            for (Province province : provinceList) {
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        }else {
            //如果没有读取到,就用接口组装出一个请求地址,然后调用queryFromServer()方法从服务器上查询数据
         String address="http://guolin.tech/api/china";
         queryFromServer(address,"province");
        }
    }

    /**
     * 查询选中省内所有的市,优先从数据库查询,如果没有查询到再去服务器上查询
     */
    private void queryCities(){
        titleText.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        //查询数据
        cityList = LitePal.where("provinceid = ?",String.valueOf(selectedProvince.getId())).find(City.class);
        //判断集合长度大于0
        if(cityList.size()>0){
            //先清空集合
            dataList.clear();
            //for循环将数据一个一个添加到集合中
            for (City city : cityList) {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        }else {
            int provinceCode = selectedProvince.getProvinceCode();
            String address = "http://guolin.tech/api/china/"+provinceCode;
            queryFromServer(address,"city");
        }
    }

    /**
     * 查询选中市内所有的县,优先从数据库查询,如果没有查询到再去服务器上查询
     */
    private void queryCounties(){
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList = LitePal.where("cityid = ?",String.valueOf(selectedCity.getId())).find(County.class);
        if(countyList.size()>0){
            dataList.clear();
            for (County county : countyList) {
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        }else {
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String address = "http://guolin.tech/api/china/"+provinceCode+"/"+cityCode;
            queryFromServer(address,"county");
        }
    }

    /**
     * 根据传入的地址和类型从服务器上查询省市县数据
     * @param address  地址
     * @param type 类型
     */
    private void queryFromServer(String address,final String type){
        showProgressDialog();
        //调用HttpUtil.sendOKHttpRequest()方法来先服务器发送请求,相应的数据会回调到onResponse()方法中
        HttpUtil.sendOKHttpRequest(address, new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseText = response.body().string();
                boolean result = false;
                if("province".equals(type)){
                    //解析和处理服务器返回的数据,并存储到数据库中
                    result = Utility.handleProvinceResponse(responseText);
                }else if("city".equals(type)){
                    result = Utility.handleCityResponse(responseText,selectedProvince.getId());
                }else if("county".equals(type)){
                    result = Utility.handleCountyResponse(responseText,selectedCity.getId());
                }

                //在上面解析和处理完服务器返回的数据之后,这里再次调用了queryProvinces()方法来重新加载省级数据,
                //由于queryProvinces()方法牵扯到了UI操作,因此必须要在主线程中调用,这里借助了runOnUiThread()
                //方法来实现从子线程切换到主线程. 现在数据库中已经存在了数据,因此调用queryProvinces()就会直接将
                //数据显示到界面上了
                if(result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if("province".equals(type)){
                                queryProvinces();
                            }else if("city".equals(type)){
                                queryCities();
                            }else if("county".equals(type)){
                                queryCounties();
                            }
                        }
                    });
                }
            }
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                //通过runOnUiThread()方法回到主线程处理逻辑
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }


    /**
     * 显示进度对话框
     */
    private void showProgressDialog(){
        if(progressDialog == null){
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    /**
     * 关闭进度对话框
     */
    private void closeProgressDialog(){
        if(progressDialog!=null){
            progressDialog.dismiss();
        }
    }
}
