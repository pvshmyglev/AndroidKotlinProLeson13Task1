package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentAloneAttachmentBinding
import ru.netology.nmedia.viewmodel.PostViewModel

class FragmentAloneAttachment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentAloneAttachmentBinding.inflate(inflater, container, false)

        val viewModel: PostViewModel by viewModels(ownerProducer = ::requireParentFragment)

        val post = viewModel.openedAttachment.value

        post?.let {
            if (!it.attachment?.url.isNullOrBlank()) {
                Glide.with(binding.attachmentContent)
                    .load(it.attachment?.url)
                    .placeholder(R.drawable.ic_avatar_empty_48dp)
                    .error(R.drawable.ic_avatar_empty_48dp)
                    .timeout(10_000)
                    .into(binding.attachmentContent)
            }

            viewModel.onCancelOpenAttachment()
        }

        return binding.root

    }

}