/*
 *  Copyright (c) 2015 The WebRTC@AnyRTC project authors. All Rights Reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 *
 */

#ifndef WEBRTC_MODULES_VIDEO_CODING_CODECS_H264_H264_DECODER_IMPL_H_
#define WEBRTC_MODULES_VIDEO_CODING_CODECS_H264_H264_DECODER_IMPL_H_

#include <memory>

#include "webrtc/modules/video_coding/codecs/h264/include/h264.h"
#include "third_party/openh264/src/codec/api/svc/codec_api.h"

#include "webrtc/common_video/include/i420_buffer_pool.h"

namespace webrtc {

class H264DecoderImpl : public H264Decoder {
 public:
   H264DecoderImpl();
  ~H264DecoderImpl() override;

  // If |codec_settings| is NULL it is ignored. If it is not NULL,
  // |codec_settings->codecType| must be |kVideoCodecH264|.
  int32_t InitDecode(const VideoCodec* codec_settings,
                     int32_t number_of_cores) override;
  int32_t Release() override;

  int32_t RegisterDecodeCompleteCallback(
      DecodedImageCallback* callback) override;

  // |missing_frames|, |fragmentation| and |render_time_ms| are ignored.
  int32_t Decode(const EncodedImage& input_image,
                 bool /*missing_frames*/,
                 const RTPFragmentationHeader* /*fragmentation*/,
                 const CodecSpecificInfo* codec_specific_info = nullptr,
                 int64_t render_time_ms = -1) override;

  const char* ImplementationName() const override;

 private:

  bool IsInitialized() const;

  // Reports statistics with histograms.
  void ReportInit();
  void ReportError();

  //I420BufferPool pool_;
  ISVCDecoder* decoder_;

  DecodedImageCallback* decoded_image_callback_;

  bool has_reported_init_;
  bool has_reported_error_;
};

}  // namespace webrtc

#endif  // WEBRTC_MODULES_VIDEO_CODING_CODECS_H264_H264_DECODER_IMPL_H_
