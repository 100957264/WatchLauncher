package com.fise.xiaoyu.DB.entity;


/**
 *  计步排行榜
 */
public class StepRanking {

	protected Long id;
    protected int ranking;
    protected int step_num;
    private int champion_id;
    private int create_time;
    private int update_time;
    private int latest_data = 0;  //最新请求的判断标志 (例如 今天请求了昨天的数据 不要在请求数据)


    public StepRanking() {

    }

    public StepRanking(Long id) {
        this.id = id;
    }

    public StepRanking(Long id, int ranking, int step_num, int champion_id, int  create_time, int update_time,int latest_data) {

    	 this.id               = id;
    	 this.ranking         = ranking;
    	 this.step_num        = step_num;
    	 this.champion_id     = champion_id;
         this.create_time     = create_time;
    	 this.update_time     = update_time;
         this.latest_data     = latest_data;
    }
 
     
    
    public Long getId() {
        return id;
    }
    public void setId(Long id) { 
        this.id = id;
    }

    public int getRanking() {
        return ranking;
    }
    public int getStep_num() {
        return step_num;
    }

    public int getChampion_id() {
        return champion_id;
    }
    public int getCreate_time() {
        return create_time;
    }

    public int getUpdate_time() {
        return update_time;
    }
    public void setRanking(int ranking) {
        this.ranking = ranking;
    }

    public void setStep_num(int step_num) {
        this.step_num = step_num;
    }
    public void setChampion_id(int champion_id) {
        this.champion_id = champion_id;
    }

    public void setCreate_time(int create_time) {
        this.create_time = create_time;
    }
    public void setUpdate_time(int update_time) {
        this.update_time = update_time;
    }

    public int getLatest_data() {
         return this.latest_data;
    }
    public void setLatest_data(int latest_data) {
        this.latest_data = latest_data;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StepRanking)) return false;

        StepRanking entity = (StepRanking) o;

        if (ranking != entity.ranking) return false;
        if (step_num != entity.step_num) return false;
        if (champion_id != entity.champion_id) return false;
        if (create_time != entity.create_time) return false;
        if (update_time != entity.update_time) return false;

        return true;
    }

}
