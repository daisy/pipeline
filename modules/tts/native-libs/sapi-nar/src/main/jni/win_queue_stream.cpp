#include "queue_stream.h"

#include <Shlwapi.h>

WinQueueStream::WinQueueStream():base_(0){
}

bool
WinQueueStream::initialize(){
	base_ = SHCreateMemStream(NULL, 0);
	if (base_ == 0)
		return false;
	base_->AddRef();
	return true;
}

IStream*
WinQueueStream::getBaseStream(){
	return base_;
}
	
void
WinQueueStream::startWritingPhase(){
	LARGE_INTEGER zero = { 0 };
	base_->Seek(zero, STREAM_SEEK_SET, NULL);
	base_->SetSize(*reinterpret_cast<ULARGE_INTEGER*>(&zero));
}
	
void
WinQueueStream::endWritingPhase(){
	LARGE_INTEGER zero = { 0 };
	base_->Seek(zero, STREAM_SEEK_SET, NULL);
}
	
int WinQueueStream::in_avail(){
	if (base_ == 0)
		return 0;
	STATSTG stats;
	base_->Stat(&stats, STATFLAG_NONAME);
	return (stats.cbSize.HighPart == 0 ? stats.cbSize.LowPart : stats.cbSize.HighPart); //agnostic to endianness
}
	
const char*
WinQueueStream::nextChunk(int* size){
	ULONG read = 0;
	base_->Read(audio_, AUDIO_CHUNK_SIZE, &read);
	if (read == 0)
		return 0;
	*size = (int) read;
	return audio_;
}
	
void WinQueueStream::dispose(){
	if (base_ != 0){
		base_->Release();
		base_ = 0;
	}
}