package com.example.musicwithfriends.Helpers;

import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

public class SnapHelperOneByOne extends LinearSnapHelper {

    @Override
    public int findTargetSnapPosition(RecyclerView.LayoutManager layoutManager, int velocityX, int velocityY) {

        //Отмена действия если layoutManager не является экземпляром RecyclerView
        if (!(layoutManager instanceof RecyclerView.SmoothScroller.ScrollVectorProvider)) {
            return RecyclerView.NO_POSITION;
        }

        //Привязка view к представлению layoutManager
        View currentView = findSnapView(layoutManager);

        //Отмена действия если view не имеет представления layoutManager
        if (currentView == null) {
            return RecyclerView.NO_POSITION;
        }

        LinearLayoutManager myLayoutManager = (LinearLayoutManager) layoutManager;

        int oldPosition = myLayoutManager.findLastVisibleItemPosition();
        int nextPosition = myLayoutManager.findFirstVisibleItemPosition();

        //Получаем позицию адаптера
        int currentPosition = layoutManager.getPosition(currentView);

        //
        if (velocityX > 30) {
            currentPosition = oldPosition;
        } else if (velocityX < 30) {
            currentPosition = nextPosition;
        }

        //Отмена действия если прокрутка неполноценна
        if (currentPosition == RecyclerView.NO_POSITION) {
            return RecyclerView.NO_POSITION;
        }

        //Получение позиции верхнего\нижнего элемента
        return currentPosition;
    }

}
