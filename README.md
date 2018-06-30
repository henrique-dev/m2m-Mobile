# Mim2Mim - Mobile (m2m)

Este projeto se encontra em atualização

### Download

Para baixar o último lançamento da aplicação, [clique aqui](https://play.google.com/store/apps/details?id=phdev.com.br.faciltransferencia).

### Descrição

Esta aplicação funciona em par com [m2m-Desktop](https://github.com/henrique-dev/m2m-Desktop). Essa versão é a mobile e
tem como objetivo o recebimento de arquivos de um computador para um smartphone através de transferência por conexão Wireless.

### Funcionamento

Ambos os dispositivos tem que estar na mesma rede. A versão desktop possui um receptor de broadcast que fica na escuta de mensagens, que são enviadas pela aplicação do smartphone. Por sua vez, o smartphone possui um transmissor de broadcast que envia um determinado número de broadcasts na rede, e simultaneamente abre uma entrada de requisições TCP por 5 segundos. 

Quando o desktop recebe uma mensagem de broadcast, o mesmo envia uma requisição de conexão através do protocolo TCP para o smartphone, e assim que o smartphone aceita, ambas as aplicações ficam pareadas e prontas para transferência de dados.

Desta forma, o conhecimento de ambas na rede é feito automaticamente.

### Uso

Esta é a tela inicial do aplicativo:

![](/rd/tela1.png)

Acima do botão **Conectar** há um campo editável de texto. O usuário pode colocar um nick que identificará o 
dispositivo na aplicação desktop.

Depois de inserido o nick, deve-se tocar no botão **Conectar**, e então o dispositivo tentará se conectar ao computador.

Após, caso a conexão seja efetuada com sucesso, o aplicativo irá para a seguinte tela:

![](/rd/tela3.png)

Onde se encontra uma lista que é atualizada a cada arquivo transferido com sucesso. É possível tocar no arquivo para abri-lo 
caso o tipo seja conhecido.

![](/rd/tela4.png)
