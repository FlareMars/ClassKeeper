package com.flaremars.classmanagers.uis;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVRelation;
import com.avos.avoscloud.GetCallback;
import com.avos.avoscloud.SaveCallback;
import com.flaremars.classmanagers.CMApplication;
import com.flaremars.classmanagers.R;
import com.flaremars.classmanagers.networkmodel.ICMAlbum;
import com.flaremars.classmanagers.model.AlbumsObject;
import com.flaremars.classmanagers.model.AppConst;
import com.flaremars.classmanagers.model.ClassObject;
import com.flaremars.classmanagers.model.FragmentConst;
import com.flaremars.classmanagers.utils.DisplayUtils;
import com.flaremars.classmanagers.utils.NormalUtils;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.Date;

public class CreateAlbumDialog extends DialogFragment {

    private static CreateAlbumDialog instance;

    public static CreateAlbumDialog getFunctionDialog(int targetClassId) {
        if (instance == null) {
            instance = new CreateAlbumDialog();
            Bundle arg = new Bundle();
            arg.putInt("classId", targetClassId);
            instance.setArguments(arg);
            instance.setStyle(DialogFragment.STYLE_NO_TITLE,0);
        }

        return instance;
    }

    public CreateAlbumDialog() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final int classId = getArguments().getInt("classId");
        View view = View.inflate(CMApplication.GLOBAL_CONTEXT, R.layout.layout_create_album_dialog, null);
        int screenWidth = DisplayUtils.INSTANCE.getSystemInfo(getActivity()).getScreenWidth();
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(screenWidth,250);
        view.setLayoutParams(layoutParams);
        final EditText newNameEditText = (EditText) view.findViewById(R.id.et_create_album_new_name);

        final TextView createBtn = (TextView) view.findViewById(R.id.tv_btn_create_new_album);
        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String name = newNameEditText.getText().toString();
                if (name.equals("")) {
                    Toast.makeText(getActivity(), "名字不能为空~", Toast.LENGTH_LONG).show();
                } else {
                    createBtn.setOnClickListener(null);
                    createBtn.setEnabled(false);
                    final ClassObject inClass = DataSupport.find(ClassObject.class, classId);
                    AVQuery<AVObject> classQuery = new AVQuery<>("CMClassObject");
                    classQuery.getInBackground(inClass.getClassID(), new GetCallback<AVObject>() {
                        @Override
                        public void done(final AVObject avObject, AVException e) {
                            if (e == null) {
                                final ICMAlbum newAlbum = new ICMAlbum();
                                newAlbum.setInClass(avObject);
                                newAlbum.setName(name);
                                newAlbum.setFirstPhotoThumbnail("");
                                newAlbum.setPhotoSize(0);
                                newAlbum.setTime(new Date());
                                newAlbum.setRealPhotoIds(new ArrayList<String>());
                                newAlbum.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(AVException e) {
                                        if (e == null) {
                                            final AlbumsObject albumsObject = new AlbumsObject();
                                            albumsObject.setInClass(inClass);
                                            albumsObject.setCreateTime(newAlbum.getCreateTime());
                                            albumsObject.setFirstPhotoThumbnail("");
                                            albumsObject.setName(name);
                                            albumsObject.setSizeOfPhotos(0);
                                            albumsObject.setNetworkId(newAlbum.getObjectId());
                                            albumsObject.save();

                                            AVRelation<ICMAlbum> albums = avObject.getRelation("albums");
                                            albums.add(newAlbum);

                                            avObject.saveInBackground(new SaveCallback() {
                                                @Override
                                                public void done(AVException e) {
                                                    if (e == null) {
                                                        Intent intent = new Intent(CMApplication.GLOBAL_CONTEXT, ContainerActivity.class);
                                                        Bundle bundle = new Bundle();
                                                        bundle.putSerializable(AppConst.TARGET_FRAGMENT, FragmentConst.FRAGMENT_PHOTOS);
                                                        bundle.putString("target_album_id", albumsObject.getNetworkId());
                                                        intent.putExtras(bundle);
                                                        startActivity(intent);

                                                        instance.dismiss();
                                                    }
                                                }
                                            });
                                        } else {
                                            NormalUtils.INSTANCE.showError(CMApplication.GLOBAL_CONTEXT,e);
                                        }
                                    }
                                });
                            } else {
                                NormalUtils.INSTANCE.showError(CMApplication.GLOBAL_CONTEXT,e);
                            }
                        }
                    });
                }
            }
        });
        return view;
    }


}
