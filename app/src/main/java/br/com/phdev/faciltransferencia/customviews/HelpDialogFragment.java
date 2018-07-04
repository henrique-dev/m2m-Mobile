package br.com.phdev.faciltransferencia.customviews;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

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
public class HelpDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Ajuda");
        builder.setMessage("O aplicativo tem como função a transferência de arquivos do computador para o smartphone.\n" +
                "Intruções rápidas de uso: \n\n" +
                "1) Abra o programa no computador. (Caso ainda não o tenha baixado, ele pode ser obtido mais abaixo)\n" +
                "2) Toque em 'conectar' no aplicativo, e espere a conexão ser efetuada.\n" +
                "3) Depois de conectado, é só transferir seus arquivos do computador para o smartphone a vontade." +
                "\n\n" +
                "Certifique-se de sempre usar as versões mais atuais de ambos.");
        builder.setPositiveButton("Voltar", null);
        builder.setNeutralButton("Obter programa", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String url = "https://github.com/henrique-dev/m2m-Desktop";
                Intent getProgramIntent = new Intent(Intent.ACTION_VIEW);
                getProgramIntent.setData(Uri.parse(url));
                startActivity(getProgramIntent);
            }
        });
        return builder.create();

    }

}
