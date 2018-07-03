package br.com.phdev.faciltransferencia.customviews;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import br.com.phdev.faciltransferencia.transfer.Archive;
import phdev.com.br.faciltransferencia.R;

/*
 * Copyright (C) 2018 Paulo Henrique Gonçalves Bacelar
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
public class ArchiveAdapter extends ArrayAdapter<Archive> {

    public ArchiveAdapter(Context context, List<Archive> objects) {
        super(context, 0, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_layout, parent, false);
        TextView archiveName = (TextView) convertView.findViewById(R.id.archiveName);
        ImageView imageView = (ImageView) convertView.findViewById(R.id.imageView);
        Archive archive = getItem(position);

        if (archive != null) {
            if (archiveName != null)
                archiveName.setText(archive.getName());
            switch (Archive.checkFileExtension(archive.getName())) {
                case IMAGE:
                    imageView.setImageResource(R.drawable.baseline_photo_library_black_48);
                    break;
                case DOCUMENT:
                    imageView.setImageResource(R.drawable.baseline_library_books_black_48);
                    break;
                case MUSIC:
                    imageView.setImageResource(R.drawable.baseline_library_music_black_48);
                    break;
                case VIDEO:
                    imageView.setImageResource(R.drawable.baseline_video_library_black_48);
                    break;
                case UNDEFINED:
                    imageView.setImageResource(R.drawable.baseline_filter_none_black_48);
                    break;
            }
        }
        return convertView;
    }
}
