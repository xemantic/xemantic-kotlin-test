const webpack = require("webpack");
const envPlugin = new webpack.DefinePlugin({
  'process': {
    'env': {
      'FOO': JSON.stringify(process.env.FOO)
    }
  }
});
config.plugins.push(envPlugin);
