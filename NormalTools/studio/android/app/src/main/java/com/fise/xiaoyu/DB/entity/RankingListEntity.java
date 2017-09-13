package com.fise.xiaoyu.DB.entity;


/**
 *  计步排行榜
 */
public class RankingListEntity {

	protected Long id;

    protected Long rankingId;
    protected int step_num;
    private int champion_id;
    private int create_time;
    private int update_time;


    public RankingListEntity() {

    }

    public RankingListEntity(Long id) {
        this.id = id;
    }

    public RankingListEntity(Long id,Long rankingId, int step_num, int champion_id,int create_time, int update_time) {

    	 this.id               = id;
         this.rankingId       = rankingId;
    	 this.step_num        = step_num;
    	 this.champion_id     = champion_id;
         this.create_time     = create_time;
         this.update_time     = update_time;

    }
 

    public Long getId() {
        return id;
    }
    public void setId(Long id) { 
        this.id = id;
    }

    public Long getRankingId() {
        return rankingId;
    }
    public void setRankingId(Long rankingId) {
        this.rankingId = rankingId;
    }

    public int getStep_num() {
        return step_num;
    }
    public void setStep_num(int step_num) {
        this.step_num = step_num;
    }


    public int getChampion_id() {
        return champion_id;
    }
    public void setChampion_id(int champion_id) {
        this.champion_id = champion_id;
    }


    public int getCreate_time() {
        return create_time;
    }
    public void setCreate_time(int create_time) {
        this.create_time = create_time;
    }


    public int getUpdate_time() {
        return update_time;
    }
    public void setUpdate_time(int update_time) {
        this.update_time = update_time;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RankingListEntity)) return false;

        RankingListEntity entity = (RankingListEntity) o;

        if (step_num != entity.step_num) return false;
        if (champion_id != entity.champion_id) return false;
        if (create_time != entity.create_time) return false;
        if (update_time != entity.update_time) return false;

        return true;
    }

}
