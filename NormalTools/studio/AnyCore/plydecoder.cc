/*
*  Copyright (c) 2016 The AnyRTC project authors. All Rights Reserved.
*
*  Please visit https://www.anyrtc.io for detail.
*
* The GNU General Public License is a free, copyleft license for
* software and other kinds of works.
*
* The licenses for most software and other practical works are designed
* to take away your freedom to share and change the works.  By contrast,
* the GNU General Public License is intended to guarantee your freedom to
* share and change all versions of a program--to make sure it remains free
* software for all its users.  We, the Free Software Foundation, use the
* GNU General Public License for most of our software; it applies also to
* any other work released this way by its authors.  You can apply it to
* your programs, too.
* See the GNU LICENSE file for more info.
*/
#include "plydecoder.h"
#include "anyrtmpcore.h"
#include "webrtc/base/logging.h"
#include "webrtc/media/engine/webrtcvideoframe.h"

#ifndef WEBRTC_WIN
//֡����
enum Frametype_e
{
    FRAME_I  = 15,
    FRAME_P  = 16,
    FRAME_B  = 17
};
//��ȡ�ֽڽṹ��
typedef struct Tag_bs_t
{
    unsigned char *p_start;                //�������׵�ַ(�����ʼ����͵�ַ)
    unsigned char *p;                      //��������ǰ�Ķ�дָ�� ��ǰ�ֽڵĵ�ַ������᲻�ϵ�++��ÿ��++������һ���µ��ֽ�
    unsigned char *p_end;                  //������β��ַ     //typedef unsigned char   uint8_t;
    int     i_left;                        // p��ָ�ֽڵ�ǰ���ж��� ��λ�� �ɶ�д count number of available(���õ�)λ
}bs_t;
/*
 �������ƣ�
 �������ܣ���ʼ���ṹ��
 ��    ����
 �� �� ֵ���޷���ֵ,void����
 ˼    ·��
 ��    �ϣ�
 
 */
void bs_init( bs_t *s, void *p_data, int i_data )
{
    s->p_start = (unsigned char *)p_data;        //�ô����p_data�׵�ַ��ʼ��p_start��ֻ������Ч���ݵ��׵�ַ
    s->p       = (unsigned char *)p_data;        //�ֽ�?�׵�ַ��һ��ʼ��p_data��ʼ����ÿ����һ�����ֽڣ����ƶ�����һ�ֽ��׵�ַ
    s->p_end   = s->p + i_data;                   //β��ַ�����һ���ֽڵ��׵�ַ?
    s->i_left  = 8;                              //��û�п�ʼ��д����ǰ�ֽ�ʣ��δ��ȡ��λ��8
}
int bs_read( bs_t *s, int i_count )
{
    static uint32_t i_mask[33] ={0x00,
        0x01,      0x03,      0x07,      0x0f,
        0x1f,      0x3f,      0x7f,      0xff,
        0x1ff,     0x3ff,     0x7ff,     0xfff,
        0x1fff,    0x3fff,    0x7fff,    0xffff,
        0x1ffff,   0x3ffff,   0x7ffff,   0xfffff,
        0x1fffff,  0x3fffff,  0x7fffff,  0xffffff,
        0x1ffffff, 0x3ffffff, 0x7ffffff, 0xfffffff,
        0x1fffffff,0x3fffffff,0x7fffffff,0xffffffff};
    /*
     �����е�Ԫ���ö����Ʊ�ʾ���£�
     
     ���裺��ʼΪ0����д��Ϊ+���Ѷ�ȡΪ-
     
     �ֽ�:       1       2       3       4
     00000000 00000000 00000000 00000000      �±�
     
     0x00:                           00000000      x[0]
     
     0x01:                           00000001      x[1]
     0x03:                           00000011      x[2]
     0x07:                           00000111      x[3]
     0x0f:                           00001111      x[4]
     
     0x1f:                           00011111      x[5]
     0x3f:                           00111111      x[6]
     0x7f:                           01111111      x[7]
     0xff:                           11111111      x[8]    1�ֽ�
     
     0x1ff:                      0001 11111111      x[9]
     0x3ff:                      0011 11111111      x[10]   i_mask[s->i_left]
     0x7ff:                      0111 11111111      x[11]
     0xfff:                      1111 11111111      x[12]   1.5�ֽ�
     
     0x1fff:                  00011111 11111111      x[13]
     0x3fff:                  00111111 11111111      x[14]
     0x7fff:                  01111111 11111111      x[15]
     0xffff:                  11111111 11111111      x[16]   2�ֽ�
     
     0x1ffff:             0001 11111111 11111111      x[17]
     0x3ffff:             0011 11111111 11111111      x[18]
     0x7ffff:             0111 11111111 11111111      x[19]
     0xfffff:             1111 11111111 11111111      x[20]   2.5�ֽ�
     
     0x1fffff:         00011111 11111111 11111111      x[21]
     0x3fffff:         00111111 11111111 11111111      x[22]
     0x7fffff:         01111111 11111111 11111111      x[23]
     0xffffff:         11111111 11111111 11111111      x[24]   3�ֽ�
     
     0x1ffffff:    0001 11111111 11111111 11111111      x[25]
     0x3ffffff:    0011 11111111 11111111 11111111      x[26]
     0x7ffffff:    0111 11111111 11111111 11111111      x[27]
     0xfffffff:    1111 11111111 11111111 11111111      x[28]   3.5�ֽ�
     
     0x1fffffff:00011111 11111111 11111111 11111111      x[29]
     0x3fffffff:00111111 11111111 11111111 11111111      x[30]
     0x7fffffff:01111111 11111111 11111111 11111111      x[31]
     0xffffffff:11111111 11111111 11111111 11111111      x[32]   4�ֽ�
     
     */
    int      i_shr;             //
    int i_result = 0;           //������Ŷ�ȡ���ĵĽ�� typedef unsigned   uint32_t;
    
    while( i_count > 0 )     //Ҫ��ȡ�ı�����
    {
        if( s->p >= s->p_end ) //�ֽ����ĵ�ǰλ��>=����β��������˱�����s�Ѿ������ˡ�
        {                       //
            break;
        }
        
        if( ( i_shr = s->i_left - i_count ) >= 0 )    //��ǰ�ֽ�ʣ���δ��λ������Ҫ��ȡ��λ���࣬�������
        {                                           //i_left��ǰ�ֽ�ʣ���δ��λ��������Ҫ��i_count���أ�i_shr=i_left-i_count�Ľ�����>=0��˵��Ҫ��ȡ�Ķ��ڵ�ǰ�ֽ���
            //i_shr>=0��˵��Ҫ��ȡ�ı��ض����ڵ�ǰ�ֽ���
            //����׶Σ�һ���ԾͶ����ˣ�Ȼ�󷵻�i_result(�˳��˺���)
            /* more in the buffer than requested */
            i_result |= ( *s->p >> i_shr )&i_mask[i_count];//��|=��:��λ��ֵ��A |= B �� A = A|B
            //|=Ӧ�������ִ�У��ѽ������i_result(��λ�����ȼ����ڸ��ϲ�����|=)
            //i_mask[i_count]���Ҳ��λ����1,�������еİ�λ�룬���԰������еĽ�����ƹ���
            //!=,��ߵ�i_result�����ȫ��0���Ҳ�������λ�򣬻��Ǹ��ƽ�������ˣ�����ü���������
            /*��ȡ�󣬸��½ṹ������ֶ�ֵ*/
            s->i_left -= i_count; //��i_left = i_left - i_count����ǰ�ֽ�ʣ���δ��λ����ԭ���ļ�ȥ��ζ�ȡ��
            if( s->i_left == 0 ) //�����ǰ�ֽ�ʣ���δ��λ��������0��˵����ǰ�ֽڶ����ˣ���Ҫ��ʼ��һ���ֽ�
            {
                s->p++;              //�ƶ�ָ�룬����p���������ֽ�Ϊ�����ƶ�ָ���
                s->i_left = 8;       //�¿�ʼ������ֽ���˵����ǰ�ֽ�ʣ���δ��λ��������8������
            }
            return( i_result );     //���ܵķ���ֵ֮һΪ��00000000 00000000 00000000 00000001 (4�ֽڳ�)
        }
        else    /* i_shr < 0 ,���ֽڵ����*/
        {
            //����׶Σ���while��һ��ѭ�������ܻ��������һ��ѭ������һ�κ����һ�ζ����ܶ�ȡ�ķ����ֽڣ������һ�ζ���3���أ��м��ȡ��2�ֽ�(��2x8����)�����һ�ζ�ȡ��1���أ�Ȼ���˳�whileѭ��
            //��ǰ�ֽ�ʣ���δ��λ������Ҫ��ȡ��λ���٣����統ǰ�ֽ���3λδ������������Ҫ��7λ
            //???�Ե�ǰ�ֽ���˵��Ҫ���ı��أ��������ұߣ����Բ�����λ��(��λ��Ŀ���ǰ�Ҫ���ı��ط��ڵ�ǰ�ֽ�����)
            /* less(���ٵ�) in the buffer than requested */
            i_result |= (*s->p&i_mask[s->i_left]) << -i_shr;    //"-i_shr"�൱��ȡ�˾���ֵ
            //|= �� << ����λ�����������ȼ���ͬ�����Դ�������˳��ִ��
            //����:int|char ������int��4�ֽڣ�char��1�ֽڣ�sizeof(int|char)��4�ֽ�
            //i_left�����8����С��0��ȡֵ��Χ��[0,8]
            i_count  -= s->i_left;   //����ȡ�ı�����������ԭi_count��ȥi_left��i_left�ǵ�ǰ�ֽ�δ�����ı�����������else�׶Σ�i_left����ĵ�ǰ�ֽ�δ���ı���ȫ�������ˣ����Լ���
            s->p++;  //��λ����һ���µ��ֽ�
            s->i_left = 8;   //��һ�����ֽ���˵��δ������λ����Ȼ��8�������ֽ�����λ��û��ȡ��
        }
    }
    
    return( i_result );//���ܵķ���ֵ֮һΪ��00000000 00000000 00000000 00000001 (4�ֽڳ�)
}
int bs_read1( bs_t *s )
{
    
    if( s->p < s->p_end )
    {
        unsigned int i_result;
        
        s->i_left--;                           //��ǰ�ֽ�δ��ȡ��λ������1λ
        i_result = ( *s->p >> s->i_left )&0x01;//��Ҫ���ı����Ƶ���ǰ�ֽ����ң�Ȼ����0x01:00000001�����߼����������ΪҪ����ֻ��һ�����أ�������ز���0����1����0000 0001��λ��Ϳ��Ե�֪�����
        if( s->i_left == 0 )                   //�����ǰ�ֽ�ʣ��δ��λ����0������˵��ǰ�ֽ�ȫ������
        {
            s->p++;                             //ָ��s->p �Ƶ���һ�ֽ�
            s->i_left = 8;                     //���ֽ��У�δ��λ����Ȼ��8λ
        }
        return i_result;                       //unsigned int
    }
    
    return 0;                                  //����0Ӧ����û�ж�������
}
int bs_read_ue( bs_t *s )
{
    int i = 0;
    
    while( bs_read1( s ) == 0 && s->p < s->p_end && i < 32 )    //����Ϊ�������ĵ�ǰ����=0��ָ��δԽ�磬���ֻ�ܶ�32����
    {
        i++;
    }
    return( ( 1 << i) - 1 + bs_read( s, i ) );
}
#endif

PlyDecoder::PlyDecoder()
	: running_(false)
	, playing_(false)
	, h264_decoder_(NULL)
	, video_render_(NULL)
	, aac_decoder_(NULL)
	, a_cache_len_(0)
	, aac_sample_hz_(44100)
	, aac_channels_(2)
	, aac_frame_per10ms_size_(0)
{
	{
		h264_decoder_ = webrtc::H264Decoder::Create();
		webrtc::VideoCodec codecSetting;
		codecSetting.codecType = webrtc::kVideoCodecH264;
		codecSetting.width = 640;
		codecSetting.height = 480;
		h264_decoder_->InitDecode(&codecSetting, 1);
		h264_decoder_->RegisterDecodeCompleteCallback(this);
		webrtc::VideoCodec setting;
		setting.width = 640;
		setting.height = 480;
		setting.codecType = webrtc::kVideoCodecH264;
		setting.maxFramerate = 30;
		if (h264_decoder_->InitDecode(&setting, 1) != 0) {
			//@AnyRTC - Error
		}
	}

	aac_frame_per10ms_size_ = (aac_sample_hz_ / 100) * sizeof(int16_t) * aac_channels_;
	running_ = true;
	rtc::Thread::Start();

	ply_buffer_ = new PlyBuffer(*this, this);
}


PlyDecoder::~PlyDecoder()
{
	running_ = false;
	rtc::Thread::Stop();

	if (ply_buffer_) {
		delete ply_buffer_;
		ply_buffer_ = NULL;
	}
	if (aac_decoder_) {
		aac_decoder_close(aac_decoder_);
		aac_decoder_ = NULL;
	}
	if (h264_decoder_) {
		delete h264_decoder_;
		h264_decoder_ = NULL;
	}
}

bool PlyDecoder::IsPlaying()
{
    if (ply_buffer_ == NULL) {
        return false;
    }
    if (ply_buffer_->PlayerStatus() == PS_Cache) {
        return false;
    }
    return true;
}

int  PlyDecoder::CacheTime()
{
    if (ply_buffer_ != NULL) {
        return ply_buffer_->GetPlayCacheTime();
    }
    return 0;
}

void PlyDecoder::AddH264Data(const uint8_t*pdata, int len, uint32_t ts)
{
	if (ply_buffer_) {
		ply_buffer_->CacheH264Data(pdata, len, ts);
	}
}
void PlyDecoder::AddAACData(const uint8_t*pdata, int len, uint32_t ts)
{
	if (ply_buffer_) {
		if (aac_decoder_ == NULL) {
			aac_decoder_ = aac_decoder_open((unsigned char*)pdata, len, &aac_channels_, &aac_sample_hz_);
			if (aac_channels_ == 0)
				aac_channels_ = 1;
			aac_frame_per10ms_size_ = (aac_sample_hz_ / 100) * sizeof(int16_t) * aac_channels_;
		}
		else {
			unsigned int outlen = 0;
			if (aac_decoder_decode_frame(aac_decoder_, (unsigned char*)pdata, len, audio_cache_ + a_cache_len_, &outlen) > 0) {
				//printf("");
				a_cache_len_ += outlen;
				int ct = 0;
				int fsize = aac_frame_per10ms_size_;
				while (a_cache_len_ > fsize) {
					ply_buffer_->CachePcmData(audio_cache_ + ct * fsize, fsize, ts);
					a_cache_len_ -= fsize;
					ct++;
				}

				memmove(audio_cache_, audio_cache_ + ct * fsize, a_cache_len_);
			}
		}
	}
}

int PlyDecoder::GetPcmData(void* audioSamples, uint32_t& samplesPerSec, size_t& nChannels)
{
	if (!playing_) {
		return 0;
	}
	samplesPerSec = aac_sample_hz_;
	nChannels = aac_channels_;
	return ply_buffer_->GetPlayAudio(audioSamples);
}

void PlyDecoder::Run()
{
	while (running_)
	{
		{// ProcessMessages
			this->ProcessMessages(1);
		}
		PlyPacket* pkt = NULL;
		{
			rtc::CritScope cs(&cs_list_h264_);
			if (lst_h264_buffer_.size() > 0)
			{
				pkt = lst_h264_buffer_.front();
				lst_h264_buffer_.pop_front();
			}
		}
		if (pkt != NULL) {
			if (h264_decoder_)
			{
				int frameType = pkt->_data[4] & 0x1f;
				webrtc::EncodedImage encoded_image;
				encoded_image._buffer = (uint8_t*)pkt->_data;
				encoded_image._length = pkt->_data_len;
				encoded_image._size = pkt->_data_len + 8;
				if (frameType == 7) {
					encoded_image._frameType = webrtc::kVideoFrameKey;
				}
				else {
					encoded_image._frameType = webrtc::kVideoFrameDelta;
				}
				encoded_image._completeFrame = true;
                webrtc::RTPFragmentationHeader frag_info;
                int ret = h264_decoder_->Decode(encoded_image, false, &frag_info);
				if (ret != 0)
				{
				}
			}
			delete pkt;
		}
	}
}

void PlyDecoder::OnPlay()
{
	playing_ = true;
}
void PlyDecoder::OnPause()
{
	playing_ = false;
}
bool PlyDecoder::OnNeedDecodeData(PlyPacket* pkt)
{
	const uint8_t*pdata = pkt->_data;
	if (pkt->_b_video) {
#ifndef WEBRTC_WIN
        bs_t s;
        bs_init(&s,pkt->_data + 4 + 1,pkt->_data_len - 4 -1);
        {
            /* i_first_mb */
            bs_read_ue( &s );
            /* picture type */
            int frame_type =  bs_read_ue( &s );
            Frametype_e ft = FRAME_P;
            switch(frame_type)
            {
                case 0: case 5: /* P */
                    ft = FRAME_P;
                    break;
                case 1: case 6: /* B */
                    ft = FRAME_B;
                    break;
                case 3: case 8: /* SP */
                    ft = FRAME_P;
                    break;
                case 2: case 7: /* I */
                    ft = FRAME_I;
                    break;
                case 4: case 9: /* SI */  
                    ft = FRAME_I;
                    break;  
            }
            
            if(ft == FRAME_B) {
                return false;
            }
        }
#endif
        int type = pdata[4] & 0x1f;
		rtc::CritScope cs(&cs_list_h264_);
		if (type == 7) {
			//* Skip all buffer data, beacause decode is so slow!!!
			std::list<PlyPacket*>::iterator iter = lst_h264_buffer_.begin();
			while (iter != lst_h264_buffer_.end()) {
				PlyPacket* plypkt = *iter;
				lst_h264_buffer_.erase(iter++);
				delete plypkt;
			}
        }
		lst_h264_buffer_.push_back(pkt);
	}

	return true;
}

int32_t PlyDecoder::Decoded(webrtc::VideoFrame& decodedImage)
{
	const cricket::WebRtcVideoFrame render_frame(
		decodedImage.video_frame_buffer(),
		decodedImage.render_time_ms() * rtc::kNumNanosecsPerMillisec, decodedImage.rotation());

	if (video_render_ != NULL) {
		video_render_->OnFrame(render_frame);
	}
	return 0;
}