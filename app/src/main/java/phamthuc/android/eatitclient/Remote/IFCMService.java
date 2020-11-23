package phamthuc.android.eatitclient.Remote;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMService {
    @Headers( {
            "Content-Type:application/json",
            "Authorization:key=AAAAvW5EK6k:APA91bElvE1lw4MVDmrrKuJftcyhJJXlqW1KfNu9QYFjAsDjHXRG4gKA8-TYfdm6g3XeEek_zVgZ0i2zmYKry7X292rbdgvLBCvhJDsFrh5f89sOTqYj0oX_-1R5S6xF05GUM-15NM5a"
    } )
    @POST("fcm/send")
    Observable<FCMResponse> sendNotification(@Body FCMSendData body);
}
