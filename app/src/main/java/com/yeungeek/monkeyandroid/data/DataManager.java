package com.yeungeek.monkeyandroid.data;

import com.yeungeek.monkeyandroid.data.local.DatabaseHelper;
import com.yeungeek.monkeyandroid.data.local.LanguageHelper;
import com.yeungeek.monkeyandroid.data.local.PreferencesHelper;
import com.yeungeek.monkeyandroid.data.model.AccessTokenResp;
import com.yeungeek.monkeyandroid.data.model.Repo;
import com.yeungeek.monkeyandroid.data.model.User;
import com.yeungeek.monkeyandroid.data.model.WrapList;
import com.yeungeek.monkeyandroid.data.remote.GithubApi;
import com.yeungeek.monkeyandroid.rxbus.RxBus;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import timber.log.Timber;

/**
 * Created by yeungeek on 2016/3/15.
 */
@Singleton
public class DataManager {
    final GithubApi githubApi;
    final RxBus rxBus;
    final PreferencesHelper preferencesHelper;
    final DatabaseHelper databaseHelper;
    final LanguageHelper languageHelper;

    @Inject
    public DataManager(final GithubApi githubApi, final RxBus rxBus,
                       final PreferencesHelper preferencesHelper, final DatabaseHelper databaseHelper,final LanguageHelper languageHelper) {
        this.githubApi = githubApi;
        this.rxBus = rxBus;
        this.preferencesHelper = preferencesHelper;
        this.databaseHelper = databaseHelper;
        this.languageHelper = languageHelper;
    }

    public Observable<User> getAccessToken(String code) {
        Observable<User> userObservable = githubApi.getOAuthToken(GithubApi.CLIENT_ID, GithubApi.CLIENT_SECRET, code).
                flatMap(new Func1<AccessTokenResp, Observable<User>>() {
                    @Override
                    public Observable<User> call(AccessTokenResp accessTokenResp) {
                        preferencesHelper.putAccessToken(accessTokenResp.getAccessToken());
                        return githubApi.getUserInfo(accessTokenResp.getAccessToken());
                    }
                }).doOnNext(new Action1<User>() {
            @Override
            public void call(User user) {
                Timber.d("### save user %s", user.getLogin());
                handleSaveUser(user);
            }
        });
        return userObservable;
    }

    public Observable<WrapList<Repo>> getRepos(final String query, final int page){
        //auth
        return githubApi.getRepos(query,page);
    }

    private void handleSaveUser(final User user) {
        preferencesHelper.putUserLogin(user.getLogin());
        preferencesHelper.putUserEmail(user.getEmail());
        preferencesHelper.putUserAvatar(user.getAvatarUrl());
    }

    public RxBus getRxBus() {
        return rxBus;
    }

    public PreferencesHelper getPreferencesHelper(){
        return preferencesHelper;
    }

    public LanguageHelper getLanguageHelper(){
        return languageHelper;
    }
}
